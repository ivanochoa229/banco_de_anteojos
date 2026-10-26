import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { donorsApi } from '../../../api/donorsApi'
import { framesApi } from '../../../api/framesApi'
import { Alert } from '../../../components/Alert'
import { Layout } from '../../../components/Layout'
import { Pagination } from '../../../components/Pagination'
import { SearchInput } from '../../../components/SearchInput'
import { Select } from '../../../components/Select'
import { useListParams } from '../../../lib/useListParams'
import { FramesTable } from '../components/FramesTable'
import { FRAME_STATUS_LABELS, FRAME_STATUS_OPTIONS } from '../labels'

export function FramesListPage() {
  const { status, q, page, setFilters, setPage } = useListParams({ status: '', q: '' })

  const framesQuery = useQuery({
    queryKey: ['frames', { status, q, page }],
    queryFn: () => framesApi.list({ status, q, page }),
    placeholderData: keepPreviousData,
  })
  const frames = framesQuery.data?.content ?? []

  // El marco solo trae donorId (el listado también lo lee el solicitante desde el probador, y el
  // nombre del donante no es para él): los nombres de la página se piden aparte, de una vez.
  const donorIds = [...new Set(frames.map((frame) => frame.donorId))].sort((a, b) => a - b)
  const donorsQuery = useQuery({
    queryKey: ['donors', { ids: donorIds }],
    queryFn: () => donorsApi.list({ ids: donorIds, size: donorIds.length }),
    enabled: donorIds.length > 0,
  })

  const donorNameById = Object.fromEntries(
    (donorsQuery.data?.content ?? []).map((donor) => [donor.id, donor.name]),
  )

  function emptyMessage() {
    if (q) return `Ningún marco coincide con «${q}».`
    if (status) return `No hay marcos en estado «${FRAME_STATUS_LABELS[status]}».`
    return 'Todavía no hay marcos en el inventario.'
  }

  return (
    <Layout>
      <div>
        <h2 className="text-xl font-semibold text-slate-900">Inventario de marcos</h2>
        <p className="mt-1 text-sm text-slate-500">
          Los marcos se cargan desde la ficha del donante que los trajo.
        </p>
      </div>

      <div className="mt-6 flex flex-wrap items-end gap-4">
        <div className="w-full max-w-xs">
          <SearchInput
            id="frame-search"
            label="Buscar"
            placeholder="Precinto"
            value={q}
            onSearch={(term) => setFilters({ q: term })}
          />
        </div>
        <div className="w-56">
          <Select
            id="status"
            label="Estado"
            options={FRAME_STATUS_OPTIONS}
            placeholder="Todos"
            value={status}
            onChange={(event) => setFilters({ status: event.target.value })}
          />
        </div>
      </div>

      <div className="mt-6">
        {framesQuery.isPending && <p className="text-slate-500">Cargando marcos…</p>}
        {framesQuery.isError && <Alert>{framesQuery.error.message}</Alert>}
        {framesQuery.data?.totalElements === 0 && (
          <p className="rounded-lg border border-dashed border-slate-300 bg-white px-6 py-10 text-center text-slate-500">
            {emptyMessage()}
          </p>
        )}
        {frames.length > 0 && <FramesTable frames={frames} donorNameById={donorNameById} />}
        <Pagination pageData={framesQuery.data} onPageChange={setPage} />
      </div>
    </Layout>
  )
}
