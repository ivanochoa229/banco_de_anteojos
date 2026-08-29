import { formatMonth } from '../../../lib/dates'

// Barras con CSS puro: el panel es chico y no justifica sumar una librería de gráficos.
export function MonthlyDeliveriesChart({ byMonth }) {
  if (byMonth.length === 0) {
    return <p className="text-sm text-slate-500">No hay entregas registradas en el período.</p>
  }

  const maxDeliveries = Math.max(...byMonth.map((entry) => entry.deliveries), 1)

  return (
    <div className="flex items-end gap-3 overflow-x-auto py-2">
      {byMonth.map((entry) => (
        <div key={entry.month} className="flex w-14 shrink-0 flex-col items-center gap-1">
          <span className="text-xs font-medium text-slate-700">{entry.deliveries}</span>
          <div
            className="w-8 rounded-t bg-sky-500"
            style={{ height: `${(entry.deliveries / maxDeliveries) * 96 + 4}px` }}
          />
          <span className="text-xs text-slate-500">{formatMonth(entry.month)}</span>
        </div>
      ))}
    </div>
  )
}
