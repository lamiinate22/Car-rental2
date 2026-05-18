import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { api } from '../api/client';
import ReservationTable from '../components/reservations/ReservationTable';
import ConfirmDialog from '../components/shared/ConfirmDialog';
import LoadingSpinner from '../components/shared/LoadingSpinner';
import './ReservationsPage.css';

export default function ReservationsPage() {
  const { user } = useAuth();
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [toDelete, setToDelete] = useState(null);

  useEffect(() => { load(); }, []);

  async function load() {
    setLoading(true);
    setError('');
    try {
      const data = await api.getReservations();
      const visible = user?.admin ? data : data.filter((r) => r.userId === user?.id);
      setReservations(visible);
    } catch {
      setError('Nie można pobrać rezerwacji. Sprawdź połączenie z backendem.');
    } finally {
      setLoading(false);
    }
  }

  async function handleDelete() {
    try {
      await api.deleteReservation(toDelete.id);
      setToDelete(null);
      load();
    } catch {
      setError('Nie udało się usunąć rezerwacji.');
      setToDelete(null);
    }
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Rezerwacje</h1>
          <p className="page-sub">
            {user?.admin ? `Wszystkie rezerwacje (${reservations.length})` : 'Twoje rezerwacje'}
          </p>
        </div>
        <Link to="/reservations/new" className="btn btn--primary">+ Nowa rezerwacja</Link>
      </div>

      {error && <p className="error-banner">{error}</p>}

      {loading ? (
        <LoadingSpinner />
      ) : reservations.length === 0 && !error ? (
        <div className="res-empty">
          <p>Brak rezerwacji.</p>
          <Link to="/reservations/new" className="btn btn--primary">Zarezerwuj samochód</Link>
        </div>
      ) : (
        <ReservationTable
          reservations={reservations}
          onDelete={(r) => { setError(''); setToDelete(r); }}
          isAdmin={user?.admin}
        />
      )}

      {toDelete && (
        <ConfirmDialog
          message={`Czy na pewno chcesz usunąć rezerwację #${toDelete.id}?`}
          onConfirm={handleDelete}
          onCancel={() => setToDelete(null)}
        />
      )}
    </div>
  );
}
