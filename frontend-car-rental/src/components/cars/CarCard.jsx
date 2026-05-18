import './CarCard.css';

export default function CarCard({ car, onEdit, onDelete, isAdmin }) {
  return (
    <div className={`car-card ${!car.availability ? 'car-card--unavailable' : ''}`}>
      <div className="car-card__header">
        <span className={`car-card__dot ${car.availability ? 'car-card__dot--ok' : 'car-card__dot--taken'}`} />
        <span className="car-card__availability">
          {car.availability ? 'Dostępny' : 'Wypożyczony'}
        </span>
      </div>

      <h3 className="car-card__brand">{car.carBrand}</h3>

      <div className="car-card__meta">
        <span className="car-card__tag">{car.colour}</span>
        <span className="car-card__tag">{car.fuel}</span>
      </div>

      <div className="car-card__stats">
        <div className="car-stat">
          <span className="car-stat__label">Cena/dzień</span>
          <span className="car-stat__value">{car.price?.toFixed(2)} zł</span>
        </div>
        <div className="car-stat">
          <span className="car-stat__label">Przebieg</span>
          <span className="car-stat__value">{car.kilometers?.toLocaleString('pl-PL')} km</span>
        </div>
        <div className="car-stat">
          <span className="car-stat__label">Zbiornik</span>
          <span className="car-stat__value">{car.fuelCapacity} L</span>
        </div>
      </div>

      {isAdmin && (
        <div className="car-card__actions">
          <button className="btn btn--ghost btn--sm" onClick={() => onEdit(car)}>Edytuj</button>
          <button className="btn btn--danger btn--sm" onClick={() => onDelete(car)}>Usuń</button>
        </div>
      )}
    </div>
  );
}
