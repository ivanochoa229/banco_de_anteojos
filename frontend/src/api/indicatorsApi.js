import { apiFetch } from '../lib/apiClient'

// Sin from/to, el backend devuelve el panel de los últimos 12 meses (RF-26/27).
export const indicatorsApi = {
  get: (from, to) => {
    const params = new URLSearchParams()
    if (from) params.set('from', from)
    if (to) params.set('to', to)
    const query = params.toString()
    return apiFetch(`/v1/indicators${query ? `?${query}` : ''}`)
  },
}
