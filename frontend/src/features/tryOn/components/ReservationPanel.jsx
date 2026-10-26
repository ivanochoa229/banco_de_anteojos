import { Link } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { assignmentsApi } from '../../../api/assignmentsApi'
import { Alert } from '../../../components/Alert'
import { formatDateTime } from '../../../lib/dates'
import { FRAME_MATERIAL_LABELS, FRAME_TYPE_LABELS } from '../../frames/labels'

const BUTTON_CLASSES =
  'rounded-lg bg-sky-700 px-4 py-2.5 text-sm font-medium text-white transition hover:bg-sky-800 disabled:cursor-not-allowed disabled:opacity-60'

function frameDescription(frame) {
  const type = FRAME_TYPE_LABELS[frame.frameType] ?? frame.frameType
  const material = FRAME_MATERIAL_LABELS[frame.material] ?? frame.material
  return `${frame.sealCode} · ${type} · ${material}`
}

/**
 * El beneficiario reserva el marco que le gustó: sale del inventario por 7 días, hasta que el
 * operador se lo asigne. Si ya tiene uno reservado, se lo muestra (y se lo puede volver a probar,
 * porque al estar reservado ya no aparece en la lista de disponibles).
 */
export function ReservationPanel({ selectedFrame, onTryOn }) {
  const queryClient = useQueryClient()

  const reservationQuery = useQuery({
    queryKey: ['me', 'reservation'],
    queryFn: assignmentsApi.getMyReservation,
  })

  // El marco cambia de estado: la lista del probador tiene que dejar de ofrecerlo (o volver a).
  function invalidate() {
    queryClient.invalidateQueries({ queryKey: ['me', 'reservation'] })
    queryClient.invalidateQueries({ queryKey: ['frames'] })
  }

  const reserveMutation = useMutation({
    mutationFn: (frameId) => assignmentsApi.reserveFrame(frameId),
    onSuccess: invalidate,
  })

  const cancelMutation = useMutation({
    mutationFn: assignmentsApi.cancelMyReservation,
    onSuccess: () => {
      reserveMutation.reset()
      invalidate()
    },
  })

  if (reservationQuery.isPending) return null
  if (reservationQuery.isError) return <Alert>{reservationQuery.error.message}</Alert>

  const current = reservationQuery.data

  if (current) {
    return (
      <div className="rounded-lg border border-emerald-200 bg-emerald-50 p-4 text-sm">
        <p className="font-semibold text-emerald-900">Tenés un marco reservado</p>
        <p className="mt-1 text-emerald-800">{frameDescription(current.frame)}</p>
        <p className="mt-1 text-emerald-800">
          Queda reservado hasta el {formatDateTime(current.reservation.expiresAt)}. Sacá un turno para
          que te lo asignen; si no, vuelve al inventario.
        </p>
        <div className="mt-3 flex flex-wrap gap-3">
          {current.frame.imageOriginalName && (
            <button type="button" className={BUTTON_CLASSES} onClick={() => onTryOn(current.frame)}>
              Probármelo
            </button>
          )}
          <button
            type="button"
            disabled={cancelMutation.isPending}
            onClick={() => cancelMutation.mutate()}
            className="rounded-lg border border-slate-300 bg-white px-4 py-2.5 text-sm font-medium text-slate-700 transition hover:bg-slate-50 disabled:opacity-60"
          >
            {cancelMutation.isPending ? 'Cancelando…' : 'Cancelar la reserva'}
          </button>
        </div>
        {cancelMutation.error && (
          <div className="mt-3">
            <Alert>{cancelMutation.error.message}</Alert>
          </div>
        )}
      </div>
    )
  }

  return (
    <div className="rounded-lg border border-slate-200 bg-white p-4 text-sm">
      <p className="font-semibold text-slate-900">¿Te gustó alguno?</p>
      <p className="mt-1 text-slate-600">
        Reservalo y queda apartado para vos durante 7 días, hasta que te lo asignen en la sede.
      </p>
      <button
        type="button"
        className={`${BUTTON_CLASSES} mt-3`}
        disabled={!selectedFrame || reserveMutation.isPending}
        onClick={() => reserveMutation.mutate(selectedFrame.id)}
      >
        {reserveMutation.isPending
          ? 'Reservando…'
          : selectedFrame
            ? `Reservar ${selectedFrame.sealCode}`
            : 'Elegí un marco para reservarlo'}
      </button>
      {reserveMutation.error && (
        <div className="mt-3 space-y-2">
          <Alert>{reserveMutation.error.message}</Alert>
          <Link to="/solicitante/mi-perfil" className="inline-block font-medium text-sky-700 hover:underline">
            Ir a Mi Perfil
          </Link>
        </div>
      )}
    </div>
  )
}
