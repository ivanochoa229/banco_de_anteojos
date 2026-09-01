import { useCallback, useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { authApi } from '../api/authApi'
import { clearStoredToken, getStoredToken, setOnUnauthorized, storeToken } from '../lib/apiClient'
import { decodeJwtPayload } from '../lib/jwt'
import { AuthContext } from './useAuth'

export function AuthProvider({ children }) {
  // Hidrata desde localStorage: el token es la única fuente de verdad (el role sale de su claim).
  const [token, setToken] = useState(() => getStoredToken())
  const navigate = useNavigate()

  const logout = useCallback(() => {
    clearStoredToken()
    setToken(null)
  }, [])

  useEffect(() => {
    setOnUnauthorized(() => {
      logout()
      navigate('/login', { replace: true })
    })
    return () => setOnUnauthorized(null)
  }, [logout, navigate])

  const login = useCallback(async (email, password) => {
    const { token: newToken } = await authApi.login(email, password)
    storeToken(newToken)
    setToken(newToken)
  }, [])

  const registerApplicant = useCallback(async (data) => {
    const { token: newToken } = await authApi.registerApplicant(data)
    storeToken(newToken)
    setToken(newToken)
  }, [])

  const value = useMemo(
    () => ({
      token,
      role: token ? (decodeJwtPayload(token)?.role ?? null) : null,
      isAuthenticated: Boolean(token),
      login,
      registerApplicant,
      logout,
    }),
    [token, login, registerApplicant, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
