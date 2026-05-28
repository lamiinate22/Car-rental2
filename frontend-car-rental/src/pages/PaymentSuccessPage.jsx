import { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { api } from '../api/client';
import LoadingSpinner from '../components/shared/LoadingSpinner';

export default function PaymentSuccessPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const sessionId = searchParams.get('session_id');

  const [status, setStatus] = useState('loading');
  const [reservationId, setReservationId] = useState(null);

  useEffect(() => {
    if (!sessionId) {
      setStatus('error');
      return;
    }

    api.confirmPayment(sessionId)
      .then((data) => {
        setReservationId(data.reservationId);
        setStatus('success');
      })
      .catch(() => setStatus('error'));
  }, [sessionId]);

  if (status === 'loading') return <LoadingSpinner />;

  if (status === 'error') {
    return (
      <div style={{ textAlign: 'center', padding: '4rem 1rem' }}>
        <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>⚠️</div>
        <h1 style={{ marginBottom: '0.5rem' }}>Nie udało się potwierdzić płatności</h1>
        <p style={{ color: '#6b7280', marginBottom: '2rem' }}>
          Jeśli środki zostały pobrane, rezerwacja zostanie wkrótce aktywowana automatycznie.
        </p>
        <button className="btn btn--primary" onClick={() => navigate('/reservations')}>
          Przejdź do rezerwacji
        </button>
      </div>
    );
  }

  return (
    <div style={{ textAlign: 'center', padding: '4rem 1rem' }}>
      <div style={{ fontSize: '4rem', marginBottom: '1rem' }}>✅</div>
      <h1 style={{ marginBottom: '0.5rem' }}>Płatność zakończona pomyślnie!</h1>
      <p style={{ color: '#6b7280', marginBottom: '0.5rem' }}>
        Twoja rezerwacja została potwierdzona.
      </p>
      {reservationId && (
        <p style={{ color: '#6b7280', marginBottom: '2rem' }}>
          Numer rezerwacji: <strong>#{reservationId}</strong>
        </p>
      )}
      <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center', flexWrap: 'wrap' }}>
        <button
          className="btn btn--primary"
          onClick={() => navigate(reservationId ? `/reservations/${reservationId}` : '/reservations')}
        >
          Zobacz rezerwację
        </button>
        <button className="btn btn--secondary" onClick={() => navigate('/')}>
          Wróć do strony głównej
        </button>
      </div>
    </div>
  );
}
