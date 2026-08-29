// Etiquetas en español de ProductStatus.
export const PRODUCT_STATUS_LABELS = {
  ACTIVE: 'Activo',
  DISCONTINUED: 'Dado de baja',
}

export const PRODUCT_STATUS_STYLES = {
  ACTIVE: 'bg-green-50 text-green-700',
  DISCONTINUED: 'bg-slate-100 text-slate-600',
}

export function isActive(product) {
  return product.status === 'ACTIVE'
}
