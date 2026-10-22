import { SHIPMENT_STATUS_STYLES, shipmentStatusLabel } from '../labels'

export function ShipmentStatusBadge({ shipment }) {
  return (
    <span
      className={`rounded-full px-2.5 py-0.5 text-xs font-medium ${
        SHIPMENT_STATUS_STYLES[shipment.status] ?? 'bg-slate-100 text-slate-600'
      }`}
    >
      {shipmentStatusLabel(shipment)}
    </span>
  )
}
