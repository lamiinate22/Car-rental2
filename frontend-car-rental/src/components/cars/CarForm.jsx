import { useState } from 'react';
import './CarForm.css';

const FUEL_TYPES = ['95', '98', 'ON', 'ON+', 'LPG'];

const empty = { carBrand: '', colour: '', price: '', kilometers: '', fuel: '95', fuelCapacity: '', availability: true };

export default function CarForm({ initial, onSubmit, onCancel, loading }) {
  const [form, setForm] = useState(initial ?? empty);

  function handleChange(e) {
    const { name, value, type, checked } = e.target;
    setForm((f) => ({ ...f, [name]: type === 'checkbox' ? checked : value }));
  }

  function handleSubmit(e) {
    e.preventDefault();
    onSubmit({
      ...form,
      price: parseFloat(form.price),
      kilometers: parseInt(form.kilometers, 10),
      fuelCapacity: parseFloat(form.fuelCapacity),
    });
  }

  return (
    <form onSubmit={handleSubmit} className="car-form">
      <div className="field">
        <label className="field__label">Marka i model</label>
        <input name="carBrand" className="field__input" value={form.carBrand} onChange={handleChange} required placeholder="np. Toyota Corolla" />
      </div>

      <div className="field-row">
        <div className="field">
          <label className="field__label">Kolor</label>
          <input name="colour" className="field__input" value={form.colour} onChange={handleChange} required placeholder="np. Czarny" />
        </div>
        <div className="field">
          <label className="field__label">Paliwo</label>
          <select name="fuel" className="field__input" value={form.fuel} onChange={handleChange}>
            {FUEL_TYPES.map((t) => <option key={t}>{t}</option>)}
          </select>
        </div>
      </div>

      <div className="field-row">
        <div className="field">
          <label className="field__label">Cena / dzień (zł)</label>
          <input name="price" type="number" step="0.01" min="0" className="field__input" value={form.price} onChange={handleChange} required />
        </div>
        <div className="field">
          <label className="field__label">Przebieg (km)</label>
          <input name="kilometers" type="number" min="0" className="field__input" value={form.kilometers} onChange={handleChange} required />
        </div>
      </div>

      <div className="field-row">
        <div className="field">
          <label className="field__label">Pojemność zbiornika (L)</label>
          <input name="fuelCapacity" type="number" step="0.1" min="0" className="field__input" value={form.fuelCapacity} onChange={handleChange} required />
        </div>
        <div className="field field--checkbox">
          <label className="checkbox-label">
            <input name="availability" type="checkbox" checked={form.availability} onChange={handleChange} />
            Dostępny
          </label>
        </div>
      </div>

      <div className="car-form__actions">
        <button type="button" className="btn btn--ghost" onClick={onCancel}>Anuluj</button>
        <button type="submit" className="btn btn--primary" disabled={loading}>
          {loading ? 'Zapisywanie…' : (initial ? 'Zapisz zmiany' : 'Dodaj samochód')}
        </button>
      </div>
    </form>
  );
}
