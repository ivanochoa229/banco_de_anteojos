const BASE_URL = import.meta.env.VITE_API_URL ?? ''
const TOKEN_KEY = 'auth_token'

export class ApiError extends Error {
  constructor(message, status) {
    super(message)
    this.status = status
  }
}

// Handler registrado por AuthContext: logout + redirect a /login cuando la sesión vence.
let onUnauthorized = null

export function setOnUnauthorized(handler) {
  onUnauthorized = handler
}

export function getStoredToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function storeToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearStoredToken() {
  localStorage.removeItem(TOKEN_KEY)
}

export async function apiFetch(path, { method = 'GET', body } = {}) {
  const token = getStoredToken()
  const isFormData = body instanceof FormData
  const headers = {}
  // Con FormData el Content-Type lo pone el navegador: necesita agregarle el boundary.
  if (body !== undefined && !isFormData) headers['Content-Type'] = 'application/json'
  if (token) headers.Authorization = `Bearer ${token}`

  let requestBody
  if (body === undefined) requestBody = undefined
  else if (isFormData) requestBody = body
  else requestBody = JSON.stringify(body)

  let response
  try {
    response = await fetch(`${BASE_URL}${path}`, { method, headers, body: requestBody })
  } catch {
    throw new ApiError('No se pudo conectar con el servidor', 0)
  }

  // Solo desloguea si había token (sesión vencida); el 401 del propio login lo maneja el formulario.
  if (response.status === 401 && token && onUnauthorized) {
    onUnauthorized()
  }

  if (!response.ok) {
    let message = 'Ocurrió un error inesperado'
    try {
      const data = await response.json()
      if (data?.message) message = data.message
    } catch {
      // respuesta sin body JSON: queda el mensaje genérico
    }
    throw new ApiError(message, response.status)
  }

  if (response.status === 204) return null
  return response.json()
}
