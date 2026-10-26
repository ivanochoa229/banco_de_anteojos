import { Link } from 'react-router-dom'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { framesApi } from '../../../api/framesApi'
import { shipmentsApi } from '../../../api/shipmentsApi'
import { Layout } from '../../../components/Layout'
import { ShipmentForm } from '../components/ShipmentForm'

export function ShipmentCreatePage() {
  const queryClient = useQueryClient()

  const createMutation = useMutation({
    mutationFn: (data) => shipmentsApi.create(data),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['shipments'] }),
  })

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
            {/* Se ofrece el inventario completo: un marco puede viajar entre sucursales en cualquier
                estado, y el envío no le cambia el estado (el inventario dice qué se puede hacer con
                el marco, no dónde está parado). El backend solo exige que exista. */}
            <ShipmentForm
              fetchFrames={framesApi.list}
              noFramesMessage={
                <>
                  No hay marcos en el inventario.{' '}
                  <Link to="/donors" className="font-medium text-sky-700 hover:underline">
                    Cargá los marcos de una donación.
                  </Link>
                </>
              }
              onSubmit={(data) => createMutation.mutate(data)}
              isPending={createMutation.isPending}
              submitError={createMutation.error?.message}
            />
          </div>
        )}
      </section>
    </Layout>
  )
}
