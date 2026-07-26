// Convención única de fechas para la UI: DD/MM/YYYY (ver CLAUDE.md).
export function formatDate(isoDate) {
  if (!isoDate) return '—'
  const [year, month, day] = isoDate.split('-')
  return `${day}/${month}/${year}`
}

// Los timestamps del backend son LocalDateTime ISO sin zona (2026-07-26T10:30:00).
export function formatDateTime(isoDateTime) {
  if (!isoDateTime) return '—'
  const [date, time = ''] = isoDateTime.split('T')
  const hoursAndMinutes = time.slice(0, 5)
  return hoursAndMinutes ? `${formatDate(date)} ${hoursAndMinutes}` : formatDate(date)
}
