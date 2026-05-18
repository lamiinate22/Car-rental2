import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import './Navbar.css';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/login');
  }

  return (
    <header className="navbar">
      <div className="navbar__inner">
        <NavLink to="/" className="navbar__brand">
          <span className="navbar__brand-icon">⬡</span>
          CarRental
        </NavLink>

        <nav className="navbar__nav">
          <NavLink to="/" className={({ isActive }) => isActive ? 'nav-link nav-link--active' : 'nav-link'} end>
            Samochody
          </NavLink>
          {user && (
            <NavLink to="/reservations" className={({ isActive }) => isActive ? 'nav-link nav-link--active' : 'nav-link'}>
              Rezerwacje
            </NavLink>
          )}
          {user && (
            <NavLink to="/fuel-prices" className={({ isActive }) => isActive ? 'nav-link nav-link--active' : 'nav-link'}>
              Ceny paliw
            </NavLink>
          )}
          {user?.admin && (
            <NavLink to="/options" className={({ isActive }) => isActive ? 'nav-link nav-link--active' : 'nav-link'}>
              Opcje
            </NavLink>
          )}
          {user?.admin && (
            <NavLink to="/users" className={({ isActive }) => isActive ? 'nav-link nav-link--active' : 'nav-link'}>
              Użytkownicy
            </NavLink>
          )}
        </nav>

        <div className="navbar__user">
          {user ? (
            <>
              <span className="navbar__username">{user.firstName} {user.lastName}</span>
              {user.admin && <span className="navbar__role">admin</span>}
              <button className="btn btn--ghost btn--sm" onClick={handleLogout}>Wyloguj</button>
            </>
          ) : (
            <NavLink to="/login" className="btn btn--primary btn--sm">Zaloguj się</NavLink>
          )}
        </div>
      </div>
    </header>
  );
}
