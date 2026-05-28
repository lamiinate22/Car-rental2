const BASE_URL = 'http://localhost:8080';

function getToken() {
  return localStorage.getItem('car_rental_token');
}

async function request(path, options = {}) {
  const token = getToken();
  const headers = { 'Content-Type': 'application/json', ...options.headers };
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const res = await fetch(`${BASE_URL}${path}`, { ...options, headers });

  if (!res.ok) {
    const err = new Error(`HTTP ${res.status}`);
    err.status = res.status;
    if (res.status === 401 && path !== '/api/auth/login') {
      localStorage.removeItem('car_rental_token');
      localStorage.removeItem('car_rental_user');
      window.location.href = '/login';
    }
    throw err;
  }
  const text = await res.text();
  return text ? JSON.parse(text) : null;
}

export const api = {
  // Auth
  login: (username, password) => request('/api/auth/login', { method: 'POST', body: JSON.stringify({ username, password }) }),
  register: (body) => request('/users/register', { method: 'POST', body: JSON.stringify(body) }),

  // Users
  getUsers: () => request('/users'),
  setAdminRole: (id, admin) => request(`/users/${id}/admin?admin=${admin}`, { method: 'PUT' }),
  deleteUser: (id) => request(`/users/${id}`, { method: 'DELETE' }),

  // Cars
  getCars: () => request('/cars'),
  getCar: (id) => request(`/cars/${id}`),
  getAvailableCars: () => request('/cars/available'),
  addCar: (body) => request('/cars', { method: 'POST', body: JSON.stringify(body) }),
  updateCar: (body) => request('/cars', { method: 'PUT', body: JSON.stringify(body) }),
  deleteCar: (id) => request(`/cars/${id}`, { method: 'DELETE' }),

  // Reservations
  getReservations: () => request('/reservations'),
  getReservation: (id) => request(`/reservations/${id}`),
  addReservation: (body) => request('/reservations', { method: 'POST', body: JSON.stringify(body) }),
  updateReservation: (id, body) => request(`/reservations/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
  deleteReservation: (id) => request(`/reservations/delete/${id}`, { method: 'DELETE' }),

  // Options
  getOptions: () => request('/options'),
  addOption: (body) => request('/options', { method: 'POST', body: JSON.stringify(body) }),
  deleteOption: (id) => request(`/options/${id}`, { method: 'DELETE' }),

  // Fuels
  getFuelPrices: () => request('/fuels/prices'),
  getFuelTypes: () => request('/fuels/types'),

  // Payments
  createCheckoutSession: (body) => request('/payments/create-checkout-session', { method: 'POST', body: JSON.stringify(body) }),
  confirmPayment: (sessionId) => request(`/payments/confirm?sessionId=${sessionId}`),
};
