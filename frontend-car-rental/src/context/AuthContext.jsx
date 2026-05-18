import { createContext, useContext, useState } from 'react';
import { api } from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try {
      const stored = localStorage.getItem('car_rental_user');
      return stored ? JSON.parse(stored) : null;
    } catch {
      return null;
    }
  });

  async function login(username, password) {
    const data = await api.login({ username, password });
    localStorage.setItem('car_rental_user', JSON.stringify(data));
    setUser(data);
    return data;
  }

  async function register(firstName, lastName, username, password) {
    const data = await api.register({ firstName, lastName, username, password });
    localStorage.setItem('car_rental_user', JSON.stringify(data));
    setUser(data);
    return data;
  }

  function logout() {
    localStorage.removeItem('car_rental_user');
    setUser(null);
  }

  return (
    <AuthContext.Provider value={{ user, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
