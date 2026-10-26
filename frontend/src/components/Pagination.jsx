const BUTTON_CLASSES =
  'rounded-lg border border-slate-300 bg-white px-3 py-1.5 font-medium text-slate-700 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-40'

/**
 * Pie de un listado paginado. Solo Anterior/Siguiente: con buscador y filtros, saltar a una
 * página puntual no es algo que el personal necesite, y así queda simple de operar (RNF-05).
 *
 * pageData: la página tal como la devuelve la API ({ page, totalPages, totalElements }).
 */
export function Pagination({ pageData, onPageChange }) {
  if (!pageData || pageData.totalElements === 0) return null
  const { page, totalPages, totalElements } = pageData

  // La URL puede apuntar a una página que ya no existe (se borraron registros, o cambió el filtro
  // en otra pestaña): se ofrece volver al principio en vez de mostrar una tabla vacía muda.
  if (page >= totalPages) {
    return (
      <div className="mt-3 flex items-center gap-3 text-sm text-slate-500">
        <span>Esta página ya no tiene resultados.</span>
        <button type="button" className={BUTTON_CLASSES} onClick={() => onPageChange(0)}>
          Ir a la primera página
        </button>
      </div>
    )
  }

  return (
    <nav
      aria-label="Paginación"
      className="mt-3 flex flex-wrap items-center justify-between gap-3 text-sm text-slate-500"
    >
      <span>
        {totalElements} {totalElements === 1 ? 'resultado' : 'resultados'}
        {totalPages > 1 && ` · Página ${page + 1} de ${totalPages}`}
      </span>
      {totalPages > 1 && (
        <div className="flex gap-2">
          <button
            type="button"
            className={BUTTON_CLASSES}
            disabled={page === 0}
            onClick={() => onPageChange(page - 1)}
          >
            ← Anterior
          </button>
          <button
            type="button"
            className={BUTTON_CLASSES}
            disabled={page >= totalPages - 1}
            onClick={() => onPageChange(page + 1)}
          >
            Siguiente →
          </button>
        </div>
      )}
    </nav>
  )
}
