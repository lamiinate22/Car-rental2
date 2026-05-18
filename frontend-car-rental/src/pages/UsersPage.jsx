import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { api } from '../api/client';
import ConfirmDialog from '../components/shared/ConfirmDialog';
import LoadingSpinner from '../components/shared/LoadingSpinner';
import './UsersPage.css';

export default function UsersPage() {
  const { user: me } = useAuth();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [toDelete, setToDelete] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => { load(); }, []);

  async function load() {
    setLoading(true);
    try { setUsers(await api.getUsers()); }
    finally { setLoading(false); }
  }

  async function handleDelete() {
    try {
      await api.deleteUser(toDelete.id);
      setToDelete(null);
      load();
    } catch {
      setError('Nie udało się usunąć użytkownika.');
      setToDelete(null);
    }
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Użytkownicy</h1>
          <p className="page-sub">{users.length} zarejestrowanych kont</p>
        </div>
      </div>

      {error && <p className="error-banner">{error}</p>}

      {loading ? <LoadingSpinner /> : (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Imię i nazwisko</th>
                <th>Login</th>
                <th>Rola</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.id}>
                  <td className="text-muted">#{u.id}</td>
                  <td>
                    <div className="user-name">{u.firstName} {u.lastName}</div>
                  </td>
                  <td className="text-muted">{u.username}</td>
                  <td>
                    {u.admin
                      ? <span className="role-badge role-badge--admin">Admin</span>
                      : <span className="role-badge">Użytkownik</span>}
                  </td>
                  <td>
                    {u.id !== me?.id && !u.admin && (
                      <button className="btn btn--danger btn--sm" onClick={() => { setError(''); setToDelete(u); }}>Usuń</button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {toDelete && (
        <ConfirmDialog
          message={`Czy na pewno chcesz usunąć użytkownika "${toDelete.username}"?`}
          onConfirm={handleDelete}
          onCancel={() => setToDelete(null)}
        />
      )}
    </div>
  );
}
