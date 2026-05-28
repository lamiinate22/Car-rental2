import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './AuthPage.css';

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ firstName: '', lastName: '', username: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  function handleChange(e) {
    setForm((f) => ({ ...f, [e.target.name]: e.target.value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await register(form.firstName, form.lastName, form.username, form.password);
      navigate('/login');
    } catch {
      setError('Rejestracja nie powiodła się. Sprawdź dane i spróbuj ponownie.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-card__header">
          <span className="auth-card__icon">⬡</span>
          <h1 className="auth-card__title">Rejestracja</h1>
          <p className="auth-card__sub">Utwórz konto</p>
        </div>

        <form onSubmit={handleSubmit} className="auth-form">
          <div className="field-row">
            <div className="field">
              <label className="field__label" htmlFor="firstName">Imię</label>
              <input id="firstName" name="firstName" className="field__input" value={form.firstName} onChange={handleChange} required />
            </div>
            <div className="field">
              <label className="field__label" htmlFor="lastName">Nazwisko</label>
              <input id="lastName" name="lastName" className="field__input" value={form.lastName} onChange={handleChange} required />
            </div>
          </div>

          <div className="field">
            <label className="field__label" htmlFor="username">Login</label>
            <input id="username" name="username" className="field__input" value={form.username} onChange={handleChange} autoComplete="username" required />
          </div>

          <div className="field">
            <label className="field__label" htmlFor="password">Hasło</label>
            <input id="password" name="password" type="password" className="field__input" value={form.password} onChange={handleChange} autoComplete="new-password" required />
          </div>

          {error && <p className="auth-form__error">{error}</p>}

          <button className="btn btn--primary btn--full" disabled={loading}>
            {loading ? 'Tworzenie konta…' : 'Zarejestruj się'}
          </button>
        </form>

        <p className="auth-card__footer">
          Masz już konto? <Link to="/login">Zaloguj się</Link>
        </p>
      </div>
    </div>
  );
}
