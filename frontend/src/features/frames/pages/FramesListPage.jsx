import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { donorsApi } from '../../../api/donorsApi'
import { framesApi } from '../../../api/framesApi'
import { Alert } from '../../../components/Alert'
import { Layout } from '../../../components/Layout'
import { Select } from '../../../components/Select'
import { FramesTable } from '../components/FramesTable'
import { FRAME_STATUS_LABELS, FRAME_STATUS_OPTIONS } from '../labels'

export function FramesListPage() {
  const [status, setStatus] = useState('')

  const framesQuery = useQuery({
    queryKey: ['frames', status],
    queryFn: () => framesApi.list(status),
  })

  // El marco solo trae donorId; el nombre sale de acá. Es una lista chica y queda cacheada.
  const donorsQuery = useQuery({
    queryKey: ['donors'],
    queryFn: donorsApi.list,
  })

  const donorNameById = Object.fromEntries(
    (donorsQuery.data ?? []).map((donor) => [donor.id, donor.name]),
  )

  const frames = framesQuery.data
    ? [...framesQuery.data].sort((a, b) => b.receivedAt.localeCompare(a.receivedAt))
    : []

  return (
    <Layout>
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-semibold text-slate-900">Inventario de marcos</h2>
          <p className="mt-1 text-sm text-slate-500">
            Los marcos se cargan desde la ficha del donante que los trajo.
          </p>
        </div>
        <div className="w-56">
          <Select
            id="status"
            label="Estado"
            options={FRAME_STATUS_OPTIONS}
            placeholder="Todos"
            value={status}
            onChange={(event) => setStatus(event.target.value)}
          />
        </div>
      </div>

      <div className="mt-6">
        {framesQuery.isPending && <p className="text-slate-500">Cargando marcos…</p>}
        {framesQuery.isError && <Alert>{framesQuery.error.message}</Alert>}
        {framesQuery.isSuccess && frames.length === 0 && (
          <p className="rounded-lg border border-dashed border-slate-300 bg-white px-6 py-10 text-center text-slate-500">
            {status
              ? `No hay marcos en estado «${FRAME_STATUS_LABELS[status]}».`
              : 'Todavía no hay marcos en el inventario.'}
          </p>
        )}
        {frames.length > 0 && (
          <>
            <p className="mb-3 text-sm text-slate-500">
              {frames.length} {frames.length === 1 ? 'marco' : 'marcos'}
            </p>
            <FramesTable frames={frames} donorNameById={donorNameById} />
          </>
        )}
      </div>
    </Layout>
  )
}
