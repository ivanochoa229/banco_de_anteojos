import { Link } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { framesApi } from '../../../api/framesApi'
import { shipmentsApi } from '../../../api/shipmentsApi'
import { Alert } from '../../../components/Alert'
import { Layout } from '../../../components/Layout'
import { FRAME_MATERIAL_LABELS, FRAME_TYPE_LABELS } from '../../frames/labels'
import { ShipmentForm } from '../components/ShipmentForm'

function frameLabel(frame) {
  const type = FRAME_TYPE_LABELS[frame.frameType] ?? frame.frameType
  const material = FRAME_MATERIAL_LABELS[frame.material] ?? frame.material
  return `${frame.sealCode} · ${type} · ${material}`
}

export function ShipmentCreatePage() {
  const queryClient = useQueryClient()

  // Se ofrece el inventario completo: un marco puede viajar entre sucursales en cualquier estado,
  // y el envío no le cambia el estado (el inventario dice qué se puede hacer con el marco, no
  // dónde está parado). El backend solo exige que exista.
  const framesQuery = useQuery({ queryKey: ['frames', ''], queryFn: () => framesApi.list('') })

  const createMutation = useMutation({
    mutationFn: (data) => shipmentsApi.create(data),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['shipments'] }),
  })

  const frames = framesQuery.data ?? []
  const created = createMutation.data

  return (
    <Layout>
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-semibold text-slate-900">Armar un paquete</h2>
          <p className="mt-1 text-sm text-slate-500">
            El paquete se arma primero y queda pendiente. El número de seguimiento se carga al
            despacharlo, que es cuando sale.
          </p>
        </div>
        <Link to="/shipments" className="text-sm font-medium text-sky-700 hover:underline">
          Volver a envíos
        </Link>
      </div>

      <section className="mt-8">
        {created ? (
          <div className="rounded-lg border border-slate-200 bg-white p-6">
            <h3 className="font-semibold text-slate-900">Paquete armado</h3>
            <p className="mt-2 text-sm text-slate-500">
              {created.originBranch} → {created.destinationBranch} · {created.frameIds.length} marco
              {created.frameIds.length === 1 ? '' : 's'}. Queda pendiente hasta que lo despaches con
              el número de Vía Cargo.
            </p>
            <div className="mt-4 flex gap-4 text-sm font-medium">
              <Link to={`/shipments/${created.id}`} className="text-sky-700 hover:underline">
                Ver el envío
              </Link>
              <Link to="/shipments" className="text-sky-700 hover:underline">
                Ir a la lista de envíos
              </Link>
            </div>
          </div>
        ) : (
          <div className="rounded-lg border border-slate-200 bg-white p-6">
            {framesQuery.isPending && <p className="text-slate-500">Cargando el inventario…</p>}
            {framesQuery.isError && <Alert>{framesQuery.error.message}</Alert>}

            {framesQuery.isSuccess && frames.length === 0 && (
              <p className="text-slate-500">
                No hay marcos en el inventario.{' '}
                <Link to="/donors" className="font-medium text-sky-700 hover:underline">
                  Cargá los marcos de una donación.
                </Link>
              </p>
            )}

            {frames.length > 0 && (
              <ShipmentForm
                frameOptions={frames.map((frame) => ({
                  value: frame.id,
                  label: frameLabel(frame),
                  status: frame.status,
                }))}
                onSubmit={(data) => createMutation.mutate(data)}
                isPending={createMutation.isPending}
                submitError={createMutation.error?.message}
              />
            )}
          </div>
        )}
      </section>
    </Layout>
  )
}
