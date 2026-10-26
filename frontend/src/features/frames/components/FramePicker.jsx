import { useState } from 'react'
import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { Alert } from '../../../components/Alert'
import { Pagination } from '../../../components/Pagination'
import { SearchInput } from '../../../components/SearchInput'
import { FRAME_MATERIAL_LABELS, FRAME_TYPE_LABELS } from '../labels'
import { FrameStatusBadge } from './FrameStatusBadge'

const PAGE_SIZE = 10

/**
 * Elegir uno o varios marcos sin traer el inventario entero: busca por precinto y muestra de a una
 * página. La selección la guarda quien lo usa (`selected`, con los marcos completos), así sobrevive
 * a cambiar la búsqueda o la página y se puede mostrar lo elegido aunque no esté a la vista.
 *
 * Checkboxes y radios, no un select: el select múltiple del navegador se opera con ctrl+click,
 * que es justo lo que no se le puede pedir al personal de la fundación (RNF-05).
 *
 * `queryKey` + `fetchPage({ q, page, size })` dicen de dónde salen los marcos (el inventario
 * disponible, los de un donante...). Sin `searchable` no hay buscador: los marcos de un donante
 * no se buscan por precinto. `emptyMessage` se muestra si no hay ningún marco que ofrecer.
 */
export function FramePicker({
  id,
  queryKey,
  fetchPage,
  selected,
  onChange,
  multiple = false,
  searchable = true,
  showStatus = false,
  error,
  emptyMessage,
}) {
  const [q, setQ] = useState('')
  const [page, setPage] = useState(0)

  const framesQuery = useQuery({
    queryKey: [...queryKey, { q, page, size: PAGE_SIZE }],
    queryFn: () => fetchPage({ q, page, size: PAGE_SIZE }),
    placeholderData: keepPreviousData,
  })
  const frames = framesQuery.data?.content ?? []
  const selectedIds = selected.map((frame) => frame.id)

  function toggle(frame) {
    if (!multiple) {
      onChange([frame])
      return
    }
    onChange(
      selectedIds.includes(frame.id)
        ? selected.filter((current) => current.id !== frame.id)
        : [...selected, frame],
    )
  }

  if (framesQuery.isPending) return <p className="text-sm text-slate-500">Cargando marcos…</p>
  if (framesQuery.isError) return <Alert>{framesQuery.error.message}</Alert>
  if (!q && framesQuery.data.totalElements === 0) {
    return <p className="text-sm text-slate-500">{emptyMessage}</p>
  }

  return (
    <div>
      {multiple && selected.length > 0 && (
        <div className="mb-3 flex flex-wrap gap-2">
          {selected.map((frame) => (
            <button
              key={frame.id}
              type="button"
              onClick={() => toggle(frame)}
              aria-label={`Quitar el marco ${frame.sealCode}`}
              className="rounded-full bg-sky-50 px-3 py-1 text-xs font-medium text-sky-800 hover:bg-sky-100"
            >
              {frame.sealCode} ×
            </button>
          ))}
        </div>
      )}
      {!multiple && selected.length > 0 && (
        <p className="mb-3 text-sm text-slate-600">
          Elegido: <span className="font-medium text-slate-900">{selected[0].sealCode}</span>
        </p>
      )}

      {searchable && (
        <div className="mb-2">
          <SearchInput
            id={`${id}-search`}
            placeholder="Buscar por precinto…"
            value={q}
            onSearch={(term) => {
              setQ(term)
              setPage(0)
            }}
          />
        </div>
      )}

      <div className={`rounded-lg border ${error ? 'border-red-400' : 'border-slate-300'}`}>
        {frames.length === 0 ? (
          <p className="px-3 py-4 text-sm text-slate-500">Ningún marco coincide con la búsqueda.</p>
        ) : (
          <ul className="divide-y divide-slate-100">
            {frames.map((frame) => (
              <li key={frame.id}>
                <label className="flex cursor-pointer items-center gap-3 px-3 py-2.5 hover:bg-slate-50">
                  <input
                    type={multiple ? 'checkbox' : 'radio'}
                    name={id}
                    checked={selectedIds.includes(frame.id)}
                    onChange={() => toggle(frame)}
                    className="h-4 w-4 accent-sky-700"
                  />
                  <span className="flex-1 text-sm text-slate-700">
                    <span className="font-medium">{frame.sealCode}</span> ·{' '}
                    {FRAME_TYPE_LABELS[frame.frameType] ?? frame.frameType} ·{' '}
                    {FRAME_MATERIAL_LABELS[frame.material] ?? frame.material}
                  </span>
                  {showStatus && <FrameStatusBadge status={frame.status} />}
                </label>
              </li>
            ))}
          </ul>
        )}
      </div>
      {error && <p className="mt-1 text-sm text-red-600">{error}</p>}
      <Pagination pageData={framesQuery.data} onPageChange={setPage} />
    </div>
  )
}
