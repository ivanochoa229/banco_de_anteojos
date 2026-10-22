import { Link, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { donorsApi } from '../../../api/donorsApi'
import { shipmentsApi } from '../../../api/shipmentsApi'
import { Alert } from '../../../components/Alert'
import { Layout } from '../../../components/Layout'
import { formatDateTime } from '../../../lib/dates'
import { FramesTable } from '../../frames/components/FramesTable'
import { InboundShipmentForm } from '../../shipments/components/InboundShipmentForm'
import { ShipmentStatusBadge } from '../../shipments/components/ShipmentStatusBadge'
import { DONOR_TYPE_LABELS } from '../labels'
import { FrameForm } from '../components/FrameForm'

export function DonorFramesPage() {
  const { donorId } = useParams()
  const queryClient = useQueryClient()

  const donorQuery = useQuery({
    queryKey: ['donors', donorId],
    queryFn: () => donorsApi.get(donorId),
  })

  const framesQuery = useQuery({
    queryKey: ['donors', donorId, 'frames'],
    queryFn: () => donorsApi.listFrames(donorId),
  })

  const createMutation = useMutation({
    mutationFn: (data) => donorsApi.createFrame(donorId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['donors', donorId, 'frames'] })
      // El marco nuevo también entra al inventario general.
      queryClient.invalidateQueries({ queryKey: ['frames'] })
    },
  })

  // No hay endpoint de donaciones por donante: se filtra la lista de entrantes, que es chica.
  const donationsQuery = useQuery({
    queryKey: ['shipments', 'INBOUND'],
    queryFn: () => shipmentsApi.list('INBOUND'),
  })

  const createShipmentMutation = useMutation({
    mutationFn: (data) => donorsApi.createShipment(donorId, data),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['shipments'] }),
  })

  const donor = donorQuery.data
  const donations = (donationsQuery.data ?? []).filter(
    (shipment) => String(shipment.donorId) === donorId,
  )
  const location = donor
    ? [donor.address, donor.city, donor.province, donor.postalCode && `CP ${donor.postalCode}`]
        .filter(Boolean)
        .join(', ')
    : ''
  // El endpoint no garantiza orden; el último cargado va primero.
  const frames = framesQuery.data
    ? [...framesQuery.data].sort((a, b) => b.receivedAt.localeCompare(a.receivedAt))
    : []

  return (
    <Layout>
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-semibold text-slate-900">Marcos donados</h2>
          {donor && (
            <p className="mt-1 text-sm text-slate-500">
              {donor.name} · {DONOR_TYPE_LABELS[donor.donorType] ?? donor.donorType}
              {donor.documentNumber ? ` · ${donor.documentNumber}` : ''}
            </p>
          )}
          {location && <p className="text-sm text-slate-500">{location}</p>}
        </div>
        <Link to="/donors" className="text-sm font-medium text-sky-700 hover:underline">
          Volver a donantes
        </Link>
      </div>

      {donorQuery.isError && (
        <div className="mt-6">
          <Alert>{donorQuery.error.message}</Alert>
        </div>
      )}

      <section className="mt-6">
        <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-500">
          Marcos recibidos
        </h3>
        <div className="mt-3">
          {framesQuery.isPending && <p className="text-slate-500">Cargando marcos…</p>}
          {framesQuery.isError && <Alert>{framesQuery.error.message}</Alert>}
          {framesQuery.isSuccess && frames.length === 0 && (
            <p className="rounded-lg border border-dashed border-slate-300 bg-white px-6 py-10 text-center text-slate-500">
              Este donante todavía no tiene marcos cargados.
            </p>
          )}
          {frames.length > 0 && <FramesTable frames={frames} />}
        </div>
      </section>

      <section className="mt-8">
        <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-500">
          Cargar un marco
        </h3>
        <p className="mt-1 text-sm text-slate-500">
          Una donación suele traer varios marcos: el formulario se vacía después de cada carga.
        </p>
        <div className="mt-3 rounded-lg border border-slate-200 bg-white p-6">
          <FrameForm
            onSubmit={(data) => createMutation.mutateAsync(data)}
            isPending={createMutation.isPending}
            submitError={createMutation.error?.message}
          />
        </div>
      </section>

      <section className="mt-8">
        <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-500">
          Donaciones por Vía Cargo
        </h3>
        <p className="mt-1 text-sm text-slate-500">
          Cuando el donante despacha con la etiqueta de la fundación, cargá la guía que te pasa: se
          registra en el seguimiento y la donación queda en camino hasta que se abra el paquete.
        </p>
        {donations.length > 0 && (
          <ul className="mt-3 divide-y divide-slate-100 rounded-lg border border-slate-200 bg-white">
            {donations.map((shipment) => (
              <li key={shipment.id} className="flex items-center justify-between gap-4 px-4 py-3">
                <Link
                  to={`/shipments/${shipment.id}`}
                  className="text-sm font-medium text-sky-700 hover:underline"
                >
                  Guía {shipment.trackingNumber}
                </Link>
                <span className="text-sm text-slate-500">
                  {formatDateTime(shipment.dispatchedAt)}
                </span>
                <ShipmentStatusBadge shipment={shipment} />
              </li>
            ))}
          </ul>
        )}
        <div className="mt-3 rounded-lg border border-slate-200 bg-white p-6">
          <InboundShipmentForm
            onSubmit={(data) => createShipmentMutation.mutateAsync(data)}
            isPending={createShipmentMutation.isPending}
            submitError={createShipmentMutation.error?.message}
          />
        </div>
      </section>
    </Layout>
  )
}
