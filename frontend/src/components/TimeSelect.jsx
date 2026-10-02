const HOURS = Array.from({ length: 24 }, (_, hour) => String(hour).padStart(2, '0'))
const MINUTES = Array.from({ length: 12 }, (_, i) => String(i * 5).padStart(2, '0'))

const SELECT_CLASS =
  'rounded-xl border bg-white px-3 py-2.5 text-sm text-slate-900 shadow-xs outline-none transition-all focus:ring-3 disabled:bg-slate-50 disabled:text-slate-400'

/**
 * Hora en formato 24 hs ("HH:MM"). No usa <input type="time"> porque el navegador la muestra en
 * AM/PM según el idioma del sistema, y la fundación trabaja con 24 hs. Minutos de a 5.
 */
export function TimeSelect({ id, label, value, onChange, error, disabled }) {
  const [hour = '', minute = ''] = value ? value.split(':') : []
  // Un fin calculado por el backend puede no caer en múltiplo de 5 (turnos de 7 min): se agrega
  // para que el select no muestre otro valor que el real.
  const minuteOptions = minute && !MINUTES.includes(minute) ? [...MINUTES, minute].sort() : MINUTES
  const borderClass = error
    ? 'border-red-400 focus:border-red-500 focus:ring-red-100'
    : 'border-slate-300 focus:border-orange-500 focus:ring-orange-500/15'

  function emit(nextHour, nextMinute) {
    onChange(`${nextHour || '00'}:${nextMinute || '00'}`)
  }

  return (
    <div>
      {label && (
        <label htmlFor={id} className="mb-1.5 block text-sm font-semibold text-slate-700">
          {label}
        </label>
      )}
      <div className="flex items-center gap-1.5">
        <select
          id={id}
          aria-label={label ? `${label} (hora)` : 'Hora'}
          className={`${SELECT_CLASS} ${borderClass} flex-1`}
          value={hour}
          disabled={disabled}
          onChange={(event) => emit(event.target.value, minute)}
        >
          {HOURS.map((option) => (
            <option key={option} value={option}>
              {option}
            </option>
          ))}
        </select>
        <span className="font-semibold text-slate-500">:</span>
        <select
          aria-label={label ? `${label} (minutos)` : 'Minutos'}
          className={`${SELECT_CLASS} ${borderClass} flex-1`}
          value={minute}
          disabled={disabled}
          onChange={(event) => emit(hour, event.target.value)}
        >
          {minuteOptions.map((option) => (
            <option key={option} value={option}>
              {option}
            </option>
          ))}
        </select>
      </div>
      {error && <p className="mt-1.5 text-xs font-medium text-red-600">{error}</p>}
    </div>
  )
}
