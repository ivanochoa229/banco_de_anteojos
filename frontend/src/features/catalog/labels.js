// Etiquetas en español de ProductStatus.
export const PRODUCT_STATUS_LABELS = {
  ACTIVE: 'A la venta',
  SOLD: 'Vendido',
  DISCONTINUED: 'Dado de baja',
}

export const PRODUCT_STATUS_STYLES = {
  ACTIVE: 'bg-green-50 text-green-700',
  SOLD: 'bg-sky-50 text-sky-700',
  DISCONTINUED: 'bg-slate-100 text-slate-600',
}

export function isActive(product) {
  return product.status === 'ACTIVE'
}
