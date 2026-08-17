import { formatDateTime } from '../../../lib/dates'
import { SHIPMENT_MILESTONES } from '../labels'

function Milestone({ label, at }) {
  const isDone = Boolean(at)

  return (
    <li className="flex gap-3">
      <span
        className={`mt-1.5 h-2.5 w-2.5 shrink-0 rounded-full ${
          isDone ? 'bg-sky-600' : 'border-2 border-slate-300 bg-white'
        }`}
      />
      <div className="pb-4">
        <p className={`text-sm font-medium ${isDone ? 'text-slate-900' : 'text-slate-400'}`}>
          {label}
        </p>
        <p className="text-sm text-slate-500">{isDone ? formatDateTime(at) : 'Pendiente'}</p>
      </div>
    </li>
  )
}

/** Los tres hitos propios del paquete. Lo que pasa entre medio lo cuenta el historial del carrier. */
export function ShipmentTimeline({ shipment }) {
  const isCancelled = shipment.status === 'CANCELLED'

  return (
    <ol className="border-l border-slate-200 pl-4">
      {SHIPMENT_MILESTONES.map(({ field, label }) => (
        <Milestone key={field} label={label} at={shipment[field]} />
      ))}
      {isCancelled && (
        <li className="flex gap-3">
          <span className="mt-1.5 h-2.5 w-2.5 shrink-0 rounded-full bg-red-500" />
          <div>
            <p className="text-sm font-medium text-red-700">Envío cancelado</p>
            {shipment.cancellationReason && (
              <p className="mt-1 text-sm text-slate-600">{shipment.cancellationReason}</p>
            )}
          </div>
        </li>
      )}
    </ol>
  )
}
