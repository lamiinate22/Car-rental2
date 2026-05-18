import CarForm from './CarForm';
import './CarModal.css';

export default function CarModal({ initial, onSubmit, onCancel, loading, error }) {
  return (
    <div className="modal-overlay" onClick={onCancel}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal__header">
          <h2 className="modal__title">{initial ? 'Edytuj samochód' : 'Dodaj samochód'}</h2>
          <button className="modal__close" onClick={onCancel}>✕</button>
        </div>
        {error && <p className="modal__error">{error}</p>}
        <CarForm initial={initial} onSubmit={onSubmit} onCancel={onCancel} loading={loading} />
      </div>
    </div>
  );
}
