import { apiFetch } from '../lib/apiClient'

export const catalogApi = {
  // Sin status devuelve el catálogo completo; con 'ACTIVE', solo lo disponible (RF-28).
  listProducts: (status) =>
    apiFetch(`/v1/catalog/products${status ? `?status=${status}` : ''}`),
  getProduct: (productId) => apiFetch(`/v1/catalog/products/${productId}`),
  createProduct: (data) => apiFetch('/v1/catalog/products', { method: 'POST', body: data }),
  updateProduct: (productId, data) =>
    apiFetch(`/v1/catalog/products/${productId}`, { method: 'PUT', body: data }),
  // Baja lógica (RF-30): no hay vuelta atrás.
  discontinueProduct: (productId) =>
    apiFetch(`/v1/catalog/products/${productId}/discontinuation`, { method: 'PUT' }),
  getProductImage: (productId) => apiFetch(`/v1/catalog/products/${productId}/image`),
  uploadProductImage: (productId, file) => {
    const body = new FormData()
    body.append('file', file)
    return apiFetch(`/v1/catalog/products/${productId}/image`, { method: 'PUT', body })
  },
  // Registra la venta y descuenta el stock en la misma operación (RF-29).
  sellProduct: (productId, data) =>
    apiFetch(`/v1/catalog/products/${productId}/sales`, { method: 'POST', body: data }),
  listSales: (productId) => apiFetch(`/v1/catalog/products/${productId}/sales`),
}
