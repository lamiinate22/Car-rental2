import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Layout from './components/layout/Layout';
import ProtectedRoute from './components/auth/ProtectedRoute';

import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import CarsPage from './pages/CarsPage';
import ReservationsPage from './pages/ReservationsPage';
import NewReservationPage from './pages/NewReservationPage';
import ReservationDetailPage from './pages/ReservationDetailPage';
import OptionsPage from './pages/OptionsPage';
import UsersPage from './pages/UsersPage';
import FuelPricesPage from './pages/FuelPricesPage';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          {/* Protected */}
          <Route element={<Layout />}>
            <Route path="/" element={<CarsPage />} />

            <Route path="/reservations" element={
              <ProtectedRoute><ReservationsPage /></ProtectedRoute>
            } />
            <Route path="/reservations/new" element={
              <ProtectedRoute><NewReservationPage /></ProtectedRoute>
            } />
            <Route path="/reservations/:id" element={
              <ProtectedRoute><ReservationDetailPage /></ProtectedRoute>
            } />

            <Route path="/fuel-prices" element={
              <ProtectedRoute><FuelPricesPage /></ProtectedRoute>
            } />

            <Route path="/options" element={
              <ProtectedRoute adminOnly><OptionsPage /></ProtectedRoute>
            } />
            <Route path="/users" element={
              <ProtectedRoute adminOnly><UsersPage /></ProtectedRoute>
            } />

            <Route path="*" element={<Navigate to="/" replace />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
