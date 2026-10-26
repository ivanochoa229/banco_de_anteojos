import { apiFetch, withQuery } from '../lib/apiClient'

export const framesApi = {
  // Paginado (page desde 0). Sin status devuelve el inventario completo; q busca por precinto y
  // withImage deja solo los que tienen foto para el probador.
  list: ({ status, q, withImage, page, size } = {}) =>
    apiFetch(withQuery('/v1/frames', { status, q, withImage, page, size })),
  get: (frameId) => apiFetch(`/v1/frames/${frameId}`),
  update: (frameId, data) => apiFetch(`/v1/frames/${frameId}`, { method: 'PUT', body: data }),
  // Baja definitiva (rotura, deterioro). Solo válida desde el inventario disponible.
  discard: (frameId) => apiFetch(`/v1/frames/${frameId}/disposal`, { method: 'PUT' }),
  // Foto del marco para el probador virtual. Subir otra reemplaza la anterior.
  uploadImage: (frameId, file) => {
    const body = new FormData()
    body.append('file', file)
    return apiFetch(`/v1/frames/${frameId}/image`, { method: 'PUT', body })
  },
  // Devuelve una URL firmada que vence: se pide en el momento de usarla, no se guarda.
  getImage: (frameId) => apiFetch(`/v1/frames/${frameId}/image`),
}
