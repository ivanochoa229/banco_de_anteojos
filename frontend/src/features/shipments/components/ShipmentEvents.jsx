import { formatDateTime } from '../../../lib/dates'
import { TRACKING_RAW_STATUS_LABELS } from '../labels'

/**
 * Historial que empujó 17TRACK (RF-25). Se muestra el estado traducido cuando lo conocemos y el
 * crudo entre paréntesis: el valor del carrier es el que quedó guardado y el que hay que poder
 * leer si algo no cierra.
 */
export function ShipmentEvents({ events }) {
  if (events.length === 0) {
    return (
      <p className="text-sm text-slate-500">
        Todavía no llegó ninguna actualización del correo. Los estados los empuja 17TRACK por
        webhook una vez despachado el paquete.
      </p>
    )
  }

  return (
    <ol className="space-y-4">
      {events.map((event, index) => (
        <li key={`${event.status}-${event.receivedAt}-${index}`} className="flex gap-3">
          <span className="mt-1.5 h-2.5 w-2.5 shrink-0 rounded-full bg-sky-600" />
          <div>
            <p className="text-sm font-medium text-slate-900">
              {TRACKING_RAW_STATUS_LABELS[event.status] ?? event.status}
              {TRACKING_RAW_STATUS_LABELS[event.status] && (
                <span className="ml-2 font-normal text-slate-400">({event.status})</span>
              )}
            </p>
            {event.description && <p className="text-sm text-slate-600">{event.description}</p>}
            <p className="text-sm text-slate-500">
              {formatDateTime(event.occurredAt)}
              {event.location && ` · ${event.location}`}
            </p>
          </div>
        </li>
      ))}
    </ol>
  )
}
