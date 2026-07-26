import { apiFetch } from '../lib/apiClient'
import { decodeJwtPayload } from '../lib/jwt'

export const authApi = {
  // El backend devuelve solo { token }; el role viene como claim dentro del JWT.
  async login(email, password) {
    const { token } = await apiFetch('/v1/auth/login', {
      method: 'POST',
      body: { email, password },
    })
    return { token, role: decodeJwtPayload(token)?.role ?? null }
  },
}
