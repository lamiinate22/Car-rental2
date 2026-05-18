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
        <StatusBadge active={reservation.status} />
      </div>

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
