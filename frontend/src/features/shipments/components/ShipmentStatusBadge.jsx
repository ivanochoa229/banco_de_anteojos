import { SHIPMENT_STATUS_LABELS, SHIPMENT_STATUS_STYLES } from '../labels'

export function ShipmentStatusBadge({ status }) {
  return (
    <span
      className={`rounded-full px-2.5 py-0.5 text-xs font-medium ${
        SHIPMENT_STATUS_STYLES[status] ?? 'bg-slate-100 text-slate-600'
      }`}
    >
      {SHIPMENT_STATUS_LABELS[status] ?? status}
    </span>
  )
}
