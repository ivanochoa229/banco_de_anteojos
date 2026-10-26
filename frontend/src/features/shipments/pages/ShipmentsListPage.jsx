import { Link } from 'react-router-dom'
import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { shipmentsApi } from '../../../api/shipmentsApi'
import { Alert } from '../../../components/Alert'
import { Button } from '../../../components/Button'
import { Layout } from '../../../components/Layout'
import { Pagination } from '../../../components/Pagination'
import { Select } from '../../../components/Select'
import { formatDateTime } from '../../../lib/dates'
import { useListParams } from '../../../lib/useListParams'
import { ShipmentActions } from '../components/ShipmentActions'
import { ShipmentStatusBadge } from '../components/ShipmentStatusBadge'
import { SHIPMENT_DIRECTION_OPTIONS, isInbound } from '../labels'

const SCOPE_OPTIONS = [
  { value: 'live', label: 'En curso' },
  { value: 'all', label: 'Todos' },
]

/** La recepción necesita elegir los marcos del donante: se hace desde el detalle de la donación. */
function ReceptionLink({ shipment }) {
  if (shipment.status === 'RECEIVED') return <span className="text-sm text-slate-400">—</span>

  return (
    <Link
      to={`/shipments/${shipment.id}`}
      className="text-sm font-medium text-sky-700 hover:underline"
    >
      Registrar recepción
    </Link>
  )
}

export function ShipmentsListPage() {
  const { scope, direction, page, setFilters, setPage } = useListParams({
    scope: 'live',
    direction: 'OUTBOUND',
  })
  const queryClient = useQueryClient()
  const showsDonations = direction === 'INBOUND'

  // "En curso" lo resuelve el backend; las donaciones ya traen el nombre del donante.
  const shipmentsQuery = useQuery({
    queryKey: ['shipments', { direction, scope, page }],
    queryFn: () => shipmentsApi.list({ direction, live: scope === 'live', page }),
    placeholderData: keepPreviousData,
  })

  function invalidateShipments() {
    queryClient.invalidateQueries({ queryKey: ['shipments'] })
  }

  const dispatchMutation = useMutation({
    mutationFn: ({ shipmentId, trackingNumber }) => shipmentsApi.dispatch(shipmentId, trackingNumber),
    onSuccess: invalidateShipments,
  })

  const cancelMutation = useMutation({
    mutationFn: ({ shipmentId, reason }) => shipmentsApi.cancel(shipmentId, reason),
    onSuccess: invalidateShipments,
  })

  function isRowPending(shipmentId) {
    return (
      (dispatchMutation.isPending && dispatchMutation.variables.shipmentId === shipmentId) ||
      (cancelMutation.isPending && cancelMutation.variables.shipmentId === shipmentId)
    )
  }

  const items = shipmentsQuery.data?.content ?? []
  const actionError = dispatchMutation.error?.message ?? cancelMutation.error?.message

  return (
    <Layout>
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-semibold text-slate-900">Envíos</h2>
          <p className="mt-1 text-sm text-slate-500">
            {showsDonations
              ? 'Donaciones que los donantes despachan por Vía Cargo. La guía se carga desde el donante y la donación se cierra al abrir el paquete.'
              : 'Paquetes de marcos entre sucursales. Al despacharlos se registran en el seguimiento de Vía Cargo y desde ahí el estado lo actualiza el correo.'}
          </p>
        </div>
        <div className="flex items-end gap-3">
          <div className="w-52">
            <Select
              id="direction"
              label="Tipo"
              options={SHIPMENT_DIRECTION_OPTIONS}
              value={direction}
              onChange={(event) => setFilters({ direction: event.target.value })}
            />
          </div>
          <div className="w-40">
            <Select
              id="scope"
              label="Mostrar"
              options={SCOPE_OPTIONS}
              value={scope}
              onChange={(event) => setFilters({ scope: event.target.value })}
            />
          </div>
          {!showsDonations && (
            <Link to="/shipments/new">
              <Button className="px-3 py-2.5 text-sm">Armar paquete</Button>
            </Link>
          )}
        </div>
      </div>

      {actionError && (
        <div className="mt-6">
          <Alert>{actionError}</Alert>
        </div>
      )}

      <div className="mt-6">
        {shipmentsQuery.isPending && <p className="text-slate-500">Cargando envíos…</p>}
        {shipmentsQuery.isError && <Alert>{shipmentsQuery.error.message}</Alert>}
        {shipmentsQuery.data?.totalElements === 0 && (
          <p className="rounded-lg border border-dashed border-slate-300 bg-white px-6 py-10 text-center text-slate-500">
            {showsDonations
              ? scope === 'live'
                ? 'No hay donaciones en camino.'
                : 'Todavía no se cargó ninguna donación por Vía Cargo.'
              : scope === 'live'
                ? 'No hay envíos en curso.'
                : 'Todavía no se armó ningún envío entre sucursales.'}
          </p>
        )}
        {items.length > 0 && (
          <div className="overflow-x-auto rounded-lg border border-slate-200 bg-white">
            <table className="w-full text-left text-sm">
              <thead className="border-b border-slate-200 bg-slate-50 text-xs uppercase text-slate-500">
                <tr>
                  <th className="px-4 py-3">{showsDonations ? 'Donante' : 'Recorrido'}</th>
                  <th className="px-4 py-3">Estado</th>
                  <th className="px-4 py-3">Seguimiento</th>
                  <th className="px-4 py-3">Marcos</th>
                  <th className="px-4 py-3">{showsDonations ? 'Guía cargada' : 'Armado'}</th>
                  <th className="px-4 py-3">Acciones</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {items.map(({ shipment, donorName }) => (
                  <tr key={shipment.id} className="hover:bg-slate-50">
                    <td className="px-4 py-3">
                      <Link
                        to={`/shipments/${shipment.id}`}
                        className="font-medium text-sky-700 hover:underline"
                      >
                        {isInbound(shipment)
                          ? (donorName ?? `Donante #${shipment.donorId}`)
                          : `${shipment.originBranch} → ${shipment.destinationBranch}`}
                      </Link>
                    </td>
                    <td className="px-4 py-3">
                      <ShipmentStatusBadge shipment={shipment} />
                    </td>
                    <td className="px-4 py-3 text-slate-600">{shipment.trackingNumber ?? '—'}</td>
                    <td className="px-4 py-3 text-slate-600">{shipment.frameIds.length}</td>
                    <td className="px-4 py-3 text-slate-600">
                      {formatDateTime(shipment.createdAt)}
                    </td>
                    <td className="px-4 py-3">
                      {isInbound(shipment) ? (
                        <ReceptionLink shipment={shipment} />
                      ) : (
                        <ShipmentActions
                          shipment={shipment}
                          isPending={isRowPending(shipment.id)}
                          onDispatch={(trackingNumber) =>
                            dispatchMutation.mutate({ shipmentId: shipment.id, trackingNumber })
                          }
                          onCancel={(reason) =>
                            cancelMutation.mutate({ shipmentId: shipment.id, reason })
                          }
                        />
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        <Pagination pageData={shipmentsQuery.data} onPageChange={setPage} />
      </div>
    </Layout>
  )
}
