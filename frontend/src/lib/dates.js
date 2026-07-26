// Convención única de fechas para la UI: DD/MM/YYYY (ver CLAUDE.md).
export function formatDate(isoDate) {
  if (!isoDate) return '—'
  const [year, month, day] = isoDate.split('-')
  return `${day}/${month}/${year}`
}
