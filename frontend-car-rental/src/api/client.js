const BASE_URL = 'http://localhost:8080';

async function request(path, options = {}) {
  const res = await fetch(`${BASE_URL}${path}`, {
    headers: { 'Content-Type': 'application/json', ...options.headers },
    ...options,
  });
  if (!res.ok) {
    const err = new Error(`HTTP ${res.status}`);
    err.status = res.status;
    throw err;
  }
  const text = await res.text();
  return text ? JSON.parse(text) : null;
}

export const api = {
  // Users
  getUsers: () => request('/users'),
  login: (body) => request('/users/login', { method: 'POST', body: JSON.stringify(body) }),
  register: (body) => request('/users/register', { method: 'POST', body: JSON.stringify(body) }),
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
};
