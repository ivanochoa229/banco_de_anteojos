import { apiFetch, withQuery } from '../lib/apiClient'

export const catalogApi = {
  // Paginado (page desde 0). Sin status devuelve el catálogo completo; con 'ACTIVE', lo que está
  // a la venta (RF-28). Cada producto trae imageUrl: una presigned URL que vence, solo para mostrar.
  listProducts: ({ status, page, size } = {}) =>
    apiFetch(withQuery('/v1/catalog/products', { status, page, size })),
  getProduct: (productId) => apiFetch(`/v1/catalog/products/${productId}`),
  createProduct: (data) => apiFetch('/v1/catalog/products', { method: 'POST', body: data }),
  updateProduct: (productId, data) =>
    apiFetch(`/v1/catalog/products/${productId}`, { method: 'PUT', body: data }),
  // Baja lógica (RF-30): no hay vuelta atrás.
  discontinueProduct: (productId) =>
    apiFetch(`/v1/catalog/products/${productId}/discontinuation`, { method: 'PUT' }),
  uploadProductImage: (productId, file) => {
    const body = new FormData()
    body.append('file', file)
    return apiFetch(`/v1/catalog/products/${productId}/image`, { method: 'PUT', body })
  },
  // Cada anteojo es único: venderlo lo marca como vendido (RF-29). Se vende una sola vez.
  sellProduct: (productId, data) =>
    apiFetch(`/v1/catalog/products/${productId}/sale`, { method: 'POST', body: data }),
  getSale: (productId) => apiFetch(`/v1/catalog/products/${productId}/sale`),
}
