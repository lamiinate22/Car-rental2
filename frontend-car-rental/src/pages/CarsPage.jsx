import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { api } from '../api/client';
import CarCard from '../components/cars/CarCard';
import CarModal from '../components/cars/CarModal';
import ConfirmDialog from '../components/shared/ConfirmDialog';
import LoadingSpinner from '../components/shared/LoadingSpinner';
import './CarsPage.css';

export default function CarsPage() {
  const { user } = useAuth();
  const [cars, setCars] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);
  const [onlyAvailable, setOnlyAvailable] = useState(false);
  const [modal, setModal] = useState(null);
  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState('');
  const [toDelete, setToDelete] = useState(null);
  const [deleteError, setDeleteError] = useState('');

  useEffect(() => { load(); }, []);

  async function load() {
    setLoading(true);
    setError(false);
    try {
      setCars(await api.getCars());
    } catch {
      setError(true);
    } finally {
      setLoading(false);
    }
  }

  async function handleSave(data) {
    setSaving(true);
    setSaveError('');
    try {
      if (modal.mode === 'add') {
        await api.addCar(data);
      } else {
        await api.updateCar({ ...data, id: modal.car.id });
      }
      setModal(null);
      load();
    } catch {
      setSaveError('Nie udało się zapisać. Spróbuj ponownie.');
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete() {
    try {
      await api.deleteCar(toDelete.id);
      setToDelete(null);
      load();
    } catch {
      setDeleteError('Nie udało się usunąć samochodu.');
      setToDelete(null);
    }
  }

  const displayed = onlyAvailable ? cars.filter((c) => c.availability) : cars;

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Samochody</h1>
          <p className="page-sub">{cars.length} pojazdów w flocie</p>
        </div>
        <div className="page-header__actions">
          <label className="toggle-label">
            <input
              type="checkbox"
              checked={onlyAvailable}
              onChange={(e) => setOnlyAvailable(e.target.checked)}
            />
            Tylko dostępne
          </label>
          {user?.admin && (
            <button className="btn btn--primary" onClick={() => { setSaveError(''); setModal({ mode: 'add' }); }}>
              + Dodaj samochód
            </button>
          )}
        </div>
      </div>

      {deleteError && <p className="error-state">{deleteError}</p>}

      {loading ? (
        <LoadingSpinner />
      ) : error ? (
        <div className="error-state">
          <p>Nie można pobrać samochodów — sprawdź czy backend działa.</p>
          <button className="btn btn--ghost" style={{ marginTop: '1rem' }} onClick={load}>Spróbuj ponownie</button>
        </div>
      ) : displayed.length === 0 ? (
        <p className="empty-state">Brak samochodów spełniających kryteria.</p>
      ) : (
        <div className="cars-grid">
          {displayed.map((car) => (
            <CarCard
              key={car.id}
              car={car}
              isAdmin={user?.admin}
              onEdit={(c) => { setSaveError(''); setModal({ mode: 'edit', car: c }); }}
              onDelete={(c) => { setDeleteError(''); setToDelete(c); }}
            />
          ))}
        </div>
      )}

      {modal && (
        <CarModal
          initial={modal.mode === 'edit' ? modal.car : null}
          onSubmit={handleSave}
          onCancel={() => setModal(null)}
          loading={saving}
          error={saveError}
        />
      )}

      {toDelete && (
        <ConfirmDialog
          message={`Czy na pewno chcesz usunąć ${toDelete.carBrand}?`}
          onConfirm={handleDelete}
          onCancel={() => setToDelete(null)}
        />
      )}
    </div>
  );
}
