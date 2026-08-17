import { Link, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { shipmentsApi } from '../../../api/shipmentsApi'
import { Alert } from '../../../components/Alert'
import { Layout } from '../../../components/Layout'
import { formatDateTime } from '../../../lib/dates'
import { FrameStatusBadge } from '../../frames/components/FrameStatusBadge'
import { FRAME_MATERIAL_LABELS, FRAME_TYPE_LABELS } from '../../frames/labels'
import { ShipmentActions } from '../components/ShipmentActions'
import { ShipmentEvents } from '../components/ShipmentEvents'
import { ShipmentStatusBadge } from '../components/ShipmentStatusBadge'
import { ShipmentTimeline } from '../components/ShipmentTimeline'

function Field({ label, children }) {
  return (
    <div className="flex justify-between gap-4">
      <dt className="text-slate-500">{label}</dt>
      <dd className="text-right font-medium text-slate-900">{children}</dd>
    </div>
  )
}

export function ShipmentDetailPage() {
  const { shipmentId } = useParams()
  const queryClient = useQueryClient()

  const detailQuery = useQuery({
    queryKey: ['shipments', shipmentId],
    queryFn: () => shipmentsApi.get(shipmentId),
  })

  function invalidateShipments() {
    queryClient.invalidateQueries({ queryKey: ['shipments'] })
  }

  const dispatchMutation = useMutation({
    mutationFn: (trackingNumber) => shipmentsApi.dispatch(shipmentId, trackingNumber),
    onSuccess: invalidateShipments,
  })

  const cancelMutation = useMutation({
    mutationFn: (reason) => shipmentsApi.cancel(shipmentId, reason),
    onSuccess: invalidateShipments,
  })

  if (detailQuery.isPending) {
    return (
      <Layout>
        <p className="text-slate-500">Cargando el envío…</p>
      </Layout>
    )
  }

  if (detailQuery.isError) {
    return (
      <Layout>
        <Alert>{detailQuery.error.message}</Alert>
      </Layout>
    )
  }

  const { shipment, frames, events } = detailQuery.data
  const actionError = dispatchMutation.error?.message ?? cancelMutation.error?.message
  const isPending = dispatchMutation.isPending || cancelMutation.isPending

  return (
    <Layout>
      <div className="flex items-center justify-between">
        <div>
          <div className="flex items-center gap-3">
            <h2 className="text-xl font-semibold text-slate-900">Envío #{shipment.id}</h2>
            <ShipmentStatusBadge status={shipment.status} />
          </div>
          <p className="mt-1 text-sm text-slate-500">
            {shipment.originBranch} → {shipment.destinationBranch}
          </p>
        </div>
        <Link to="/shipments" className="text-sm font-medium text-sky-700 hover:underline">
          Volver a envíos
        </Link>
      </div>

      {actionError && (
        <div className="mt-6">
          <Alert>{actionError}</Alert>
        </div>
      )}

      <div className="mt-6">
        <ShipmentActions
          shipment={shipment}
          isPending={isPending}
          onDispatch={(trackingNumber) => dispatchMutation.mutate(trackingNumber)}
          onCancel={(reason) => cancelMutation.mutate(reason)}
        />
      </div>

      <div className="mt-8 grid grid-cols-1 gap-4 lg:grid-cols-2">
        <div className="space-y-4">
          <div className="rounded-lg border border-slate-200 bg-white p-5">
            <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-500">
              Recorrido
            </h3>
            <div className="mt-4">
              <ShipmentTimeline shipment={shipment} />
            </div>
            {shipment.notes && (
              <p className="mt-2 border-t border-slate-100 pt-3 text-sm text-slate-600">
                <span className="font-medium text-slate-700">Observaciones:</span> {shipment.notes}
              </p>
            )}
          </div>

          <div className="rounded-lg border border-slate-200 bg-white p-5">
            <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-500">
              Historial del correo
            </h3>
            <div className="mt-4">
              <ShipmentEvents events={events} />
            </div>
          </div>
        </div>

        <div className="space-y-4">
          <div className="rounded-lg border border-slate-200 bg-white p-5">
            <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-500">Paquete</h3>
            <dl className="mt-3 space-y-2 text-sm">
              <Field label="Seguimiento">{shipment.trackingNumber ?? 'Sin despachar'}</Field>
              <Field label="Despachado">{formatDateTime(shipment.dispatchedAt)}</Field>
              <Field label="Entregado">{formatDateTime(shipment.deliveredAt)}</Field>
              <Field label="Marcos">{frames.length}</Field>
            </dl>
          </div>

          <div className="rounded-lg border border-slate-200 bg-white p-5">
            <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-500">
              Marcos que viajan
            </h3>
            <ul className="mt-3 divide-y divide-slate-100">
              {frames.map((frame) => (
                <li key={frame.id} className="flex items-center justify-between gap-4 py-2.5">
                  <span className="text-sm text-slate-700">
                    {frame.sealCode} · {FRAME_TYPE_LABELS[frame.frameType] ?? frame.frameType} ·{' '}
                    {FRAME_MATERIAL_LABELS[frame.material] ?? frame.material}
                  </span>
                  <FrameStatusBadge status={frame.status} />
                </li>
              ))}
            </ul>
          </div>
        </div>
      </div>
    </Layout>
  )
}
