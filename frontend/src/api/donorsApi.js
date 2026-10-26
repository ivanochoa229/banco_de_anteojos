import { apiFetch, withQuery } from '../lib/apiClient'

export const donorsApi = {
  // Paginado (page desde 0). q busca por nombre; ids trae esos donantes (para mostrar de quién
  // vino cada marco del inventario, que no trae el nombre).
  list: ({ q, ids, page, size } = {}) => apiFetch(withQuery('/v1/donors', { q, ids, page, size })),
  get: (donorId) => apiFetch(`/v1/donors/${donorId}`),
  create: (data) => apiFetch('/v1/donors', { method: 'POST', body: data }),
  // Los marcos siempre entran por un donante: no hay alta de marco suelta.
  listFrames: (donorId, { status, page, size } = {}) =>
    apiFetch(withQuery(`/v1/donors/${donorId}/frames`, { status, page, size })),
  createFrame: (donorId, data) =>
    apiFetch(`/v1/donors/${donorId}/frames`, { method: 'POST', body: data }),
  // Donación que el donante despacha por Vía Cargo: el backend registra la guía en 17TRACK.
  createShipment: (donorId, data) =>
    apiFetch(`/v1/donors/${donorId}/shipments`, { method: 'POST', body: data }),
  listShipments: (donorId, { page, size } = {}) =>
    apiFetch(withQuery(`/v1/donors/${donorId}/shipments`, { page, size })),
}
