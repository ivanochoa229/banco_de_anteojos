import { apiFetch } from '../lib/apiClient'

export const donorsApi = {
  list: () => apiFetch('/v1/donors'),
  get: (donorId) => apiFetch(`/v1/donors/${donorId}`),
  create: (data) => apiFetch('/v1/donors', { method: 'POST', body: data }),
  // Los marcos siempre entran por un donante: no hay alta de marco suelta.
  listFrames: (donorId) => apiFetch(`/v1/donors/${donorId}/frames`),
  createFrame: (donorId, data) =>
    apiFetch(`/v1/donors/${donorId}/frames`, { method: 'POST', body: data }),
  // Donación que el donante despacha por Vía Cargo: el backend registra la guía en 17TRACK.
  createShipment: (donorId, data) =>
    apiFetch(`/v1/donors/${donorId}/shipments`, { method: 'POST', body: data }),
}
