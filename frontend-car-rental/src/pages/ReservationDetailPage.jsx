import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api } from '../api/client';
import StatusBadge from '../components/shared/StatusBadge';
import LoadingSpinner from '../components/shared/LoadingSpinner';
import './ReservationDetailPage.css';

export default function ReservationDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [reservation, setReservation] = useState(null);
  const [car, setCar] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);
  const [timeLeft, setTimeLeft] = useState(null);

  useEffect(() => {
    api.getReservation(id)
      .then((res) => {
        setReservation(res);
        return api.getCar(res.carId);
      })
      .then(setCar)
      .catch(() => setError(true))
      .finally(() => setLoading(false));
  }, [id]);

  useEffect(() => {
    if (reservation?.paymentStatus !== 'PENDING' || !reservation?.stripeSessionExpiresAt) return;

    function tick() {
      const diff = new Date(reservation.stripeSessionExpiresAt) - Date.now();
      if (diff <= 0) {
        setTimeLeft({ expired: true, minutes: 0, seconds: 0 });
      } else {
        setTimeLeft({
          expired: false,
          minutes: Math.floor(diff / 60000),
          seconds: Math.floor((diff % 60000) / 1000),
        });
      }
    }

    tick();
    const interval = setInterval(tick, 1000);
    return () => clearInterval(interval);
  }, [reservation]);

  if (loading) return <LoadingSpinner />;
  if (error) return <p className="empty-state">Nie udało się załadować rezerwacji. Sprawdź połączenie z backendem.</p>;
  if (!reservation) return <p className="empty-state">Nie znaleziono rezerwacji.</p>;

  const days = Math.max(0, Math.round(
    (new Date(reservation.endDate) - new Date(reservation.startDate)) / 86400000
  ));

  return (
    <div className="detail-page">
      <button className="detail-back" onClick={() => navigate(-1)}>← Wróć</button>

      <div className="detail-header">
        <div>
          <h1 className="page-title">Rezerwacja #{reservation.id}</h1>
          <p className="page-sub">{days} {days === 1 ? 'dzień' : 'dni'} · {reservation.startDate} → {reservation.endDate}</p>
        </div>
        <StatusBadge active={reservation.status} paymentStatus={reservation.paymentStatus} />
      </div>

      {reservation.paymentStatus === 'PENDING' && (
        <div className="payment-card">
          <div className="payment-card__header">
            <span className="payment-card__icon">💳</span>
            <h2 className="payment-card__title">Dokończ płatność</h2>
          </div>

          {timeLeft?.expired ? (
            <p className="payment-card__expired">
              Sesja płatności wygasła. Wróć do listy i utwórz nową rezerwację.
            </p>
          ) : (
            <>
              <div className="payment-card__countdown">
                <span className="payment-card__countdown-label">Pozostały czas</span>
                <span className={`payment-card__countdown-time${timeLeft?.minutes < 5 ? ' payment-card__countdown-time--urgent' : ''}`}>
                  {String(timeLeft?.minutes ?? '--').padStart(2, '0')}:{String(timeLeft?.seconds ?? '--').padStart(2, '0')}
                </span>
              </div>
              {reservation.stripeSessionUrl && (
                <a
                  href={reservation.stripeSessionUrl}
                  className="btn btn--primary btn--full"
                >
                  Przejdź do płatności →
                </a>
              )}
            </>
          )}
        </div>
      )}

      <div className="detail-grid">
        <div className="detail-card">
          <h2 className="detail-card__title">Szczegóły</h2>
          <dl className="detail-dl">
            <div className="detail-dl__row">
              <dt>Samochód</dt>
              <dd>{car ? `${car.carBrand} (${car.colour})` : `#${reservation.carId}`}</dd>
            </div>
            <div className="detail-dl__row">
              <dt>Paliwo</dt>
              <dd>{car?.fuel ?? '—'}</dd>
            </div>
            <div className="detail-dl__row">
              <dt>ID użytkownika</dt>
              <dd>#{reservation.userId}</dd>
            </div>
            <div className="detail-dl__row">
              <dt>Data od</dt>
              <dd>{reservation.startDate}</dd>
            </div>
            <div className="detail-dl__row">
              <dt>Data do</dt>
              <dd>{reservation.endDate}</dd>
            </div>
            <div className="detail-dl__row">
              <dt>Kwota</dt>
              <dd className="detail-dl__price">{reservation.totalPrice?.toFixed(2)} zł</dd>
            </div>
          </dl>
        </div>

        {reservation.optionNames?.length > 0 && (
          <div className="detail-card">
            <h2 className="detail-card__title">Opcje dodatkowe</h2>
            <ul className="detail-options">
              {reservation.optionNames.map((name) => (
                <li key={name} className="detail-option">{name}</li>
              ))}
            </ul>
          </div>
        )}
      </div>
    </div>
  );
}
