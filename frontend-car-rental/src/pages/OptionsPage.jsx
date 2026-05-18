import { useEffect, useState } from 'react';
import { api } from '../api/client';
import ConfirmDialog from '../components/shared/ConfirmDialog';
import LoadingSpinner from '../components/shared/LoadingSpinner';
import './OptionsPage.css';

export default function OptionsPage() {
  const [options, setOptions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState('');
  const [toDelete, setToDelete] = useState(null);
  const [form, setForm] = useState({ name: '', price: '' });
  const [saving, setSaving] = useState(false);
  const [addError, setAddError] = useState('');
  const [deleteError, setDeleteError] = useState('');

  useEffect(() => { load(); }, []);

  async function load() {
    setLoading(true);
    setLoadError('');
    try { setOptions(await api.getOptions()); }
    catch { setLoadError('Nie udało się pobrać opcji.'); }
    finally { setLoading(false); }
  }

  async function handleAdd(e) {
    e.preventDefault();
    setSaving(true);
    setAddError('');
    try {
      await api.addOption({ name: form.name, price: parseFloat(form.price) });
      setForm({ name: '', price: '' });
      load();
    } catch {
      setAddError('Nie udało się dodać opcji.');
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete() {
    try {
      await api.deleteOption(toDelete.id);
      setToDelete(null);
      load();
    } catch {
      setDeleteError('Nie udało się usunąć opcji.');
      setToDelete(null);
    }
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Opcje dodatkowe</h1>
          <p className="page-sub">Zarządzanie dostępnymi dodatkami</p>
        </div>
      </div>

      {loadError && <p className="error-banner">{loadError}</p>}
      {deleteError && <p className="error-banner">{deleteError}</p>}

      <div className="options-layout">
        <div className="table-wrap">
          {loading ? <LoadingSpinner /> : (
            <table className="table">
              <thead>
                <tr>
                  <th>Nazwa</th>
                  <th>Cena / dzień</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {options.map((o) => (
                  <tr key={o.id}>
                    <td>{o.name}</td>
                    <td>{o.price} zł</td>
                    <td>
                      <button className="btn btn--danger btn--sm" onClick={() => { setDeleteError(''); setToDelete(o); }}>Usuń</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        <div className="options-add-card">
          <h2 className="options-add-card__title">Dodaj opcję</h2>
          <form onSubmit={handleAdd} className="options-form">
            <div className="field">
              <label className="field__label">Nazwa</label>
              <input className="field__input" value={form.name} onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))} required placeholder="np. Fotelik dziecięcy" />
            </div>
            <div className="field">
              <label className="field__label">Cena / dzień (zł)</label>
              <input type="number" step="0.01" min="0" className="field__input" value={form.price} onChange={(e) => setForm((f) => ({ ...f, price: e.target.value }))} required />
            </div>
            {addError && <p className="options-error">{addError}</p>}
            <button type="submit" className="btn btn--primary btn--full" disabled={saving}>
              {saving ? 'Dodawanie…' : 'Dodaj'}
            </button>
          </form>
        </div>
      </div>

      {toDelete && (
        <ConfirmDialog
          message={`Czy na pewno chcesz usunąć opcję "${toDelete.name}"?`}
          onConfirm={handleDelete}
          onCancel={() => setToDelete(null)}
        />
      )}
    </div>
  );
}
