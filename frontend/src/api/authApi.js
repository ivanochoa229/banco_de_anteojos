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
  // El registro público devuelve el mismo shape que login: queda logueado al registrarse.
  async registerApplicant(data) {
    const { token } = await apiFetch('/v1/auth/register', { method: 'POST', body: data })
    return { token, role: decodeJwtPayload(token)?.role ?? null }
  },
}
