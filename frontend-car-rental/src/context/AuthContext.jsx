import { createContext, useContext, useState } from 'react';
import { api } from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try {
      const token = localStorage.getItem('car_rental_token');
      const stored = localStorage.getItem('car_rental_user');
      return token && stored ? JSON.parse(stored) : null;
    } catch {
      return null;
    }
  });

  async function login(email, password) {
    const data = await api.login({ email, password });
    // data = { token, email, admin }
    localStorage.setItem('car_rental_token', data.token);
    const userObj = { email: data.email, admin: data.admin };
    localStorage.setItem('car_rental_user', JSON.stringify(userObj));
    setUser(userObj);
    return userObj;
  }

  async function register(firstName, lastName, username, password) {
    await api.register({ firstName, lastName, username, password });
    // backend nie zwraca tokena przy rejestracji - przekieruj do logowania
  }

  function logout() {
    localStorage.removeItem('car_rental_token');
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
