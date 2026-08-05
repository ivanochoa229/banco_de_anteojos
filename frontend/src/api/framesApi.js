import { apiFetch } from '../lib/apiClient'

export const framesApi = {
  // Sin status devuelve el inventario completo.
  list: (status) => apiFetch(status ? `/v1/frames?status=${status}` : '/v1/frames'),
}
