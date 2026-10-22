import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Alert } from '../../../components/Alert'
import { Button } from '../../../components/Button'
import { formatDateTime } from '../../../lib/dates'
import { FRAME_MATERIAL_LABELS, FRAME_TYPE_LABELS } from '../../frames/labels'

/**
 * Cierre de una donación entrante: el operador abre el paquete, carga cada marco bajo el donante
 * (con su precinto) y acá marca cuáles vinieron en esta donación. Solo se ofrecen los disponibles:
 * un marco asignado o entregado ya tuvo otra historia y no puede estar llegando ahora.
 *
 * Confirmar sin marcos también vale: el paquete pudo no traer nada aprovechable.
 */
export function ShipmentReceptionForm({ donorId, frames, onSubmit, isPending, submitError }) {
  const [selectedIds, setSelectedIds] = useState([])

  const availableFrames = frames
    .filter((frame) => frame.status === 'AVAILABLE')
    .sort((a, b) => b.receivedAt.localeCompare(a.receivedAt))

  function toggle(frameId) {
    setSelectedIds((current) =>
      current.includes(frameId) ? current.filter((id) => id !== frameId) : [...current, frameId],
    )
  }

  return (
    <form
      onSubmit={(event) => {
        event.preventDefault()
        onSubmit(selectedIds)
      }}
      className="space-y-4"
    >
      <p className="text-sm text-slate-600">
        Al abrir el paquete, cargá cada marco con su precinto en{' '}
        <Link to={`/donors/${donorId}/frames`} className="font-medium text-sky-700 hover:underline">
          los marcos del donante
        </Link>{' '}
        y después marcá acá los que llegaron en esta donación.
      </p>

      {availableFrames.length === 0 ? (
        <p className="rounded-lg border border-dashed border-slate-300 px-4 py-6 text-center text-sm text-slate-500">
          El donante no tiene marcos disponibles cargados.
        </p>
      ) : (
        <ul className="divide-y divide-slate-100 rounded-lg border border-slate-200">
          {availableFrames.map((frame) => (
            <li key={frame.id}>
              <label className="flex cursor-pointer items-center gap-3 px-4 py-2.5 hover:bg-slate-50">
                <input
                  type="checkbox"
                  checked={selectedIds.includes(frame.id)}
                  onChange={() => toggle(frame.id)}
                  className="h-4 w-4 accent-sky-700"
                />
                <span className="text-sm text-slate-700">
                  <span className="font-medium">{frame.sealCode}</span> ·{' '}
                  {FRAME_TYPE_LABELS[frame.frameType] ?? frame.frameType} ·{' '}
                  {FRAME_MATERIAL_LABELS[frame.material] ?? frame.material}
                </span>
                <span className="ml-auto text-xs text-slate-400">
                  Cargado {formatDateTime(frame.receivedAt)}
                </span>
              </label>
            </li>
          ))}
        </ul>
      )}

      {submitError && <Alert>{submitError}</Alert>}

      <Button type="submit" disabled={isPending}>
        {isPending
          ? 'Registrando…'
          : selectedIds.length === 0
            ? 'Confirmar recepción sin marcos'
            : `Confirmar recepción (${selectedIds.length} marco${selectedIds.length === 1 ? '' : 's'})`}
      </Button>
    </form>
  )
}
