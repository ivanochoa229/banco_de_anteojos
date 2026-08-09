import { APPOINTMENT_STATUS_LABELS, APPOINTMENT_STATUS_STYLES } from '../labels'

export function AppointmentStatusBadge({ status }) {
  return (
    <span
      className={`rounded-full px-2.5 py-0.5 text-xs font-medium ${
        APPOINTMENT_STATUS_STYLES[status] ?? 'bg-slate-100 text-slate-600'
      }`}
    >
      {APPOINTMENT_STATUS_LABELS[status] ?? status}
    </span>
  )
}
