import { useSearchParams } from 'react-router-dom'

/**
 * Página, búsqueda y filtros de un listado guardados en la URL: al volver desde un detalle (o al
 * recargar) el listado queda donde estaba. `defaults` define qué claves maneja y su valor inicial;
 * las que coinciden con el default no se escriben, para que la URL quede limpia.
 *
 * La página va en la URL contando desde 1 (como la lee una persona) y sale contando desde 0
 * (como la pide la API).
 */
export function useListParams(defaults) {
  const [searchParams, setSearchParams] = useSearchParams()

  const values = Object.fromEntries(
    Object.entries(defaults).map(([key, fallback]) => [key, searchParams.get(key) ?? fallback]),
  )
  const urlPage = Number.parseInt(searchParams.get('page') ?? '1', 10)
  const page = Number.isInteger(urlPage) && urlPage > 0 ? urlPage - 1 : 0

  // Cambiar un filtro vuelve a la primera página: la página 5 de otro filtro puede no existir.
  // Todo en un solo setSearchParams: dos seguidos en el mismo render se pisan.
  function setFilters(changes) {
    setSearchParams(
      (current) => {
        const next = new URLSearchParams(current)
        for (const [key, value] of Object.entries(changes)) {
          if (value === defaults[key]) next.delete(key)
          else next.set(key, value)
        }
        next.delete('page')
        return next
      },
      { replace: true },
    )
  }

  function setPage(nextPage) {
    setSearchParams(
      (current) => {
        const next = new URLSearchParams(current)
        if (nextPage === 0) next.delete('page')
        else next.set('page', String(nextPage + 1))
        return next
      },
      { replace: true },
    )
  }

  return { ...values, page, setFilters, setPage }
}
