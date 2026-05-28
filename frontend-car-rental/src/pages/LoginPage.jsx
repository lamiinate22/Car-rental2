import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './AuthPage.css';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: '', password: '' });
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
      await login(form.username, form.password);
      navigate('/');
    } catch (err) {
      setError(err.status === 401 ? 'Nieprawidłowy login lub hasło.' : 'Błąd serwera. Spróbuj ponownie.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-card__header">
          <span className="auth-card__icon">⬡</span>
          <h1 className="auth-card__title">Zaloguj się</h1>
          <p className="auth-card__sub">Witaj z powrotem</p>
        </div>

        <form onSubmit={handleSubmit} className="auth-form">
          <div className="field">
            <label className="field__label" htmlFor="username">Login</label>
            <input
              id="username"
              name="username"
              type="text"
              className="field__input"
              value={form.username}
              onChange={handleChange}
              autoComplete="username"
              required
            />
          </div>

          <div className="field">
            <label className="field__label" htmlFor="password">Hasło</label>
            <input
              id="password"
              name="password"
              type="password"
              className="field__input"
              value={form.password}
              onChange={handleChange}
              autoComplete="current-password"
              required
            />
          </div>

          {error && <p className="auth-form__error">{error}</p>}

          <button className="btn btn--primary btn--full" disabled={loading}>
            {loading ? 'Logowanie…' : 'Zaloguj się'}
          </button>
        </form>

        <p className="auth-card__footer">
          Nie masz konta? <Link to="/register">Zarejestruj się</Link>
        </p>
      </div>
    </div>
  );
}
