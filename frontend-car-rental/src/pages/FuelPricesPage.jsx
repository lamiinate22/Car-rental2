import { useEffect, useState } from 'react';
import { api } from '../api/client';
import LoadingSpinner from '../components/shared/LoadingSpinner';
import './FuelPricesPage.css';

export default function FuelPricesPage() {
  const [prices, setPrices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);
  const [filter, setFilter] = useState('');

  useEffect(() => {
    api.getFuelPrices()
      .then(setPrices)
      .catch(() => setError(true))
      .finally(() => setLoading(false));
  }, []);

  const fuelTypes = [...new Set(prices.map((p) => p.fuelType))].sort();
  const filtered = filter ? prices.filter((p) => p.fuelType === filter) : prices;

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Ceny paliw</h1>
          <p className="page-sub">Aktualne ceny według regionów (źródło: autocentrum.pl)</p>
        </div>
        {fuelTypes.length > 0 && (
          <div className="fuel-filter">
            <button className={`fuel-chip ${!filter ? 'fuel-chip--active' : ''}`} onClick={() => setFilter('')}>Wszystkie</button>
            {fuelTypes.map((t) => (
              <button key={t} className={`fuel-chip ${filter === t ? 'fuel-chip--active' : ''}`} onClick={() => setFilter(t)}>{t}</button>
            ))}
          </div>
        )}
      </div>

      {loading ? (
        <div className="fuel-loading">
          <LoadingSpinner />
          <p className="fuel-loading__note">Pobieranie cen może chwilę potrwać — dane są pobierane na żywo.</p>
        </div>
      ) : error ? (
        <div className="fuel-error">
          <p>Nie udało się pobrać cen paliw.</p>
          <p className="fuel-error__hint">Sprawdź połączenie backendu z internetem — endpoint korzysta ze scrapingu autocentrum.pl.</p>
        </div>
      ) : prices.length === 0 ? (
        <p className="empty-state">Brak danych o cenach paliw.</p>
      ) : (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>Region</th>
                <th>Paliwo</th>
                <th>Cena</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((p, i) => (
                <tr key={i}>
                  <td>{p.region}</td>
                  <td><span className="fuel-tag">{p.fuelType}</span></td>
                  <td className="fuel-price">{p.price?.toFixed(2)} zł/l</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
