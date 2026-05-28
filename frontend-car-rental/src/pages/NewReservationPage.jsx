import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { api } from '../api/client';
import OptionCheckbox from '../components/reservations/OptionCheckbox';
import LoadingSpinner from '../components/shared/LoadingSpinner';
import './NewReservationPage.css';

export default function NewReservationPage() {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [cars, setCars] = useState([]);
  const [options, setOptions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const [carId, setCarId] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [selectedOptions, setSelectedOptions] = useState([]);

  useEffect(() => {
    Promise.all([api.getAvailableCars(), api.getOptions()])
      .then(([c, o]) => { setCars(c); setOptions(o); })
      .finally(() => setLoading(false));
  }, []);

  const selectedCar = cars.find((c) => c.id === Number(carId));

  const days = startDate && endDate
    ? Math.max(0, Math.round((new Date(endDate) - new Date(startDate)) / 86400000))
    : 0;

  const optionsTotal = options
    .filter((o) => selectedOptions.includes(o.name))
    .reduce((sum, o) => sum + (o.price ?? 0), 0);

  const totalPrice = days > 0 && selectedCar
    ? days * (selectedCar.price ?? 0) + days * optionsTotal
    : 0;

  function toggleOption(name, checked) {
    setSelectedOptions((prev) =>
      checked ? [...prev, name] : prev.filter((n) => n !== name)
    );
  }

  async function handleSubmit(e) {
    e.preventDefault();
    if (!carId || !startDate || !endDate || days <= 0) {
      setError('Wypełnij wszystkie pola i upewnij się, że daty są poprawne.');
      return;
    }
    setSaving(true);
    setError('');
    try {
      const selectedOptionIds = options
        .filter((o) => selectedOptions.includes(o.name))
        .map((o) => o.id);

      const { sessionUrl } = await api.createCheckoutSession({
        userId: user.id,
        carId: Number(carId),
        startDate,
        endDate,
        optionIds: selectedOptionIds,
      });

      window.location.href = sessionUrl;
    } catch {
      setError('Nie udało się zainicjować płatności. Spróbuj ponownie.');
      setSaving(false);
    }
  }

  if (loading) return <LoadingSpinner />;

  return (
    <div className="new-res-page">
      <div className="page-header">
        <div>
          <h1 className="page-title">Nowa rezerwacja</h1>
          <p className="page-sub">Wybierz samochód, daty i opcje dodatkowe</p>
        </div>
      </div>

      <div className="new-res-layout">
        <form onSubmit={handleSubmit} className="new-res-form">

          <section className="form-section">
            <h2 className="form-section__title">Samochód</h2>
            <div className="field">
              <label className="field__label">Wybierz samochód</label>
              <select className="field__input" value={carId} onChange={(e) => setCarId(e.target.value)} required>
                <option value="">— wybierz —</option>
                {cars.map((c) => (
                  <option key={c.id} value={c.id}>{c.carBrand} — {c.colour} — {c.price} zł/dzień</option>
                ))}
              </select>
            </div>
          </section>

          <section className="form-section">
            <h2 className="form-section__title">Termin</h2>
            <div className="field-row">
              <div className="field">
                <label className="field__label">Data od</label>
                <input type="date" className="field__input" value={startDate} min={new Date().toISOString().split('T')[0]} onChange={(e) => setStartDate(e.target.value)} required />
              </div>
              <div className="field">
                <label className="field__label">Data do</label>
                <input type="date" className="field__input" value={endDate} min={startDate} onChange={(e) => setEndDate(e.target.value)} required />
              </div>
            </div>
          </section>

          {options.length > 0 && (
            <section className="form-section">
              <h2 className="form-section__title">Opcje dodatkowe</h2>
              <div className="options-list">
                {options.map((o) => (
                  <OptionCheckbox
                    key={o.id}
                    option={o}
                    checked={selectedOptions.includes(o.name)}
                    onChange={toggleOption}
                  />
                ))}
              </div>
            </section>
          )}

          {error && <p className="form-error">{error}</p>}

          <button type="submit" className="btn btn--primary btn--full" disabled={saving}>
            {saving ? 'Przekierowanie do płatności…' : 'Zapłać i zarezerwuj'}
          </button>
        </form>

        <aside className="res-summary">
          <h2 className="res-summary__title">Podsumowanie</h2>

          {selectedCar ? (
            <div className="res-summary__car">
              <p className="res-summary__brand">{selectedCar.carBrand}</p>
              <p className="res-summary__detail">{selectedCar.colour} · {selectedCar.fuel}</p>
            </div>
          ) : (
            <p className="res-summary__empty">Nie wybrano samochodu</p>
          )}

          <div className="res-summary__lines">
            <div className="res-summary__line">
              <span>Dni</span>
              <span>{days}</span>
            </div>
            {selectedCar && (
              <div className="res-summary__line">
                <span>Cena auta/dzień</span>
                <span>{selectedCar.price?.toFixed(2)} zł</span>
              </div>
            )}
            {selectedOptions.length > 0 && (
              <div className="res-summary__line">
                <span>Opcje/dzień</span>
                <span>{optionsTotal.toFixed(2)} zł</span>
              </div>
            )}
          </div>

          <div className="res-summary__total">
            <span>Razem</span>
            <span>{totalPrice.toFixed(2)} zł</span>
          </div>
        </aside>
      </div>
    </div>
  );
}
