import { PRODUCT_STATUS_LABELS, PRODUCT_STATUS_STYLES } from '../labels'

export function ProductStatusBadge({ status }) {
  return (
    <span
      className={`rounded-full px-2.5 py-1 text-xs font-medium ${
        PRODUCT_STATUS_STYLES[status] ?? 'bg-slate-100 text-slate-600'
      }`}
    >
      {PRODUCT_STATUS_LABELS[status] ?? status}
    </span>
  )
}
