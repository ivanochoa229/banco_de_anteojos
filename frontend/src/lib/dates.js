// Convención única de fechas para la UI: DD/MM/YYYY (ver CLAUDE.md).
export function formatDate(isoDate) {
  if (!isoDate) return '—'
  const [year, month, day] = isoDate.split('-')
  return `${day}/${month}/${year}`
}

// Fecha local de hoy en YYYY-MM-DD (para inputs date y el filtro de agenda).
// No usar toISOString(): devuelve UTC y a la noche en ART ya es el día siguiente.
export function todayIsoDate() {
  const now = new Date()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${now.getFullYear()}-${month}-${day}`
}

const MONTH_LABELS = [
  'ene',
  'feb',
  'mar',
  'abr',
  'may',
  'jun',
  'jul',
  'ago',
  'sep',
  'oct',
  'nov',
  'dic',
]

// El backend serializa YearMonth como "2026-07" (panel de indicadores, RF-26/27).
export function formatMonth(yearMonth) {
  if (!yearMonth) return '—'
  const [year, month] = yearMonth.split('-')
  return `${MONTH_LABELS[Number(month) - 1]} ${year}`
}

// Los timestamps del backend son LocalDateTime ISO sin zona (2026-07-26T10:30:00).
export function formatDateTime(isoDateTime) {
  if (!isoDateTime) return '—'
  const [date, time = ''] = isoDateTime.split('T')
  const hoursAndMinutes = time.slice(0, 5)
  return hoursAndMinutes ? `${formatDate(date)} ${hoursAndMinutes}` : formatDate(date)
}

// LocalTime del backend ("09:00:00") → "09:00".
export function formatTime(isoTime) {
  if (!isoTime) return '—'
  return isoTime.slice(0, 5)
}

const WEEKDAY_LABELS = ['Domingo', 'Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado']

// Se arma la fecha con componentes locales: new Date('YYYY-MM-DD') la toma como UTC y en ART
// caería en el día anterior.
export function formatWeekday(isoDate) {
  if (!isoDate) return '—'
  const [year, month, day] = isoDate.split('-').map(Number)
  return WEEKDAY_LABELS[new Date(year, month - 1, day).getDay()]
}
