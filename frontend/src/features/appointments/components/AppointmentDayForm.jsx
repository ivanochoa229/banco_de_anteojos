import { useState } from 'react'
import { Alert } from '../../../components/Alert'
import { Button } from '../../../components/Button'
import { Input } from '../../../components/Input'
import { todayIsoDate } from '../../../lib/dates'

const EMPTY_VALUES = { date: '', startTime: '09:00', slotDurationMinutes: '15', slotCount: '16' }

// "HH:MM" + minutos → "HH:MM", o null si se pasa de medianoche (el backend lo rechaza igual).
function endTimeOf(startTime, totalMinutes) {
  if (!startTime || !Number.isFinite(totalMinutes)) return null
  const [hours, minutes] = startTime.split(':').map(Number)
  const end = hours * 60 + minutes + totalMinutes
  if (end > 24 * 60) return null
  const pad = (value) => String(value).padStart(2, '0')
  return `${pad(Math.floor(end / 60) % 24)}:${pad(end % 60)}`
}

/**
 * Alta y edición de un día de atención. Con turnos ya dados (`isBooked`) solo se puede tocar la
 * cantidad: mover fecha, inicio o duración le cambiaría la hora a gente ya citada.
 */
export function AppointmentDayForm({
  initialDay,
  isBooked = false,
  onSubmit,
  onCancel,
  isPending,
  submitError,
  submitLabel,
}) {
  const [values, setValues] = useState(
    initialDay
      ? {
          date: initialDay.date,
          startTime: initialDay.startTime.slice(0, 5),
          slotDurationMinutes: String(initialDay.slotDurationMinutes),
          slotCount: String(initialDay.slotCount),
        }
      : EMPTY_VALUES,
  )
  const [fieldErrors, setFieldErrors] = useState({})

  function setField(name) {
    return (event) => setValues((current) => ({ ...current, [name]: event.target.value }))
  }

  const duration = Number(values.slotDurationMinutes)
  const count = Number(values.slotCount)
  const endTime = endTimeOf(values.startTime, duration * count)

  function handleSubmit(event) {
    event.preventDefault()

    const errors = {
      date: !values.date
        ? 'Elegí la fecha'
        : !isBooked && values.date < todayIsoDate()
          ? 'La fecha no puede estar en el pasado'
          : null,
      startTime: !values.startTime ? 'Elegí la hora de inicio' : null,
      slotDurationMinutes:
        !Number.isInteger(duration) || duration < 5 || duration > 240
          ? 'Entre 5 y 240 minutos'
          : null,
      slotCount: !Number.isInteger(count) || count < 1 || count > 200 ? 'Entre 1 y 200 turnos' : null,
    }
    if (!errors.startTime && !errors.slotDurationMinutes && !errors.slotCount && !endTime) {
      errors.slotCount = 'Los turnos no pueden pasar de la medianoche'
    }
    setFieldErrors(errors)
    if (Object.values(errors).some(Boolean)) return

    onSubmit({
      date: values.date,
      startTime: values.startTime,
      slotDurationMinutes: duration,
      slotCount: count,
    })
  }

  return (
    <form onSubmit={handleSubmit} noValidate className="space-y-4">
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <Input
          id="date"
          label="Fecha"
          type="date"
          min={isBooked ? undefined : todayIsoDate()}
          disabled={isBooked}
          value={values.date}
          onChange={setField('date')}
          error={fieldErrors.date}
        />
        <Input
          id="startTime"
          label="Primer turno"
          type="time"
          disabled={isBooked}
          value={values.startTime}
          onChange={setField('startTime')}
          error={fieldErrors.startTime}
        />
        <Input
          id="slotDurationMinutes"
          label="Minutos por turno"
          type="number"
          min={5}
          max={240}
          disabled={isBooked}
          value={values.slotDurationMinutes}
          onChange={setField('slotDurationMinutes')}
          error={fieldErrors.slotDurationMinutes}
        />
        <Input
          id="slotCount"
          label="Cantidad de turnos"
          type="number"
          min={1}
          max={200}
          value={values.slotCount}
          onChange={setField('slotCount')}
          error={fieldErrors.slotCount}
        />
      </div>

      <p className="text-sm text-slate-500">
        {endTime
          ? `Se atiende de ${values.startTime} a ${endTime}.`
          : 'Completá los datos para ver el horario.'}
        {isBooked &&
          ' Este día ya tiene turnos dados: solo se puede cambiar la cantidad (sin dejar afuera a nadie).'}
      </p>

      {submitError && <Alert>{submitError}</Alert>}

      <div className="flex items-center gap-4">
        <Button type="submit" disabled={isPending}>
          {isPending ? 'Guardando…' : submitLabel}
        </Button>
        {onCancel && (
          <button
            type="button"
            onClick={onCancel}
            className="text-sm font-medium text-slate-500 hover:underline"
          >
            Volver
          </button>
        )}
      </div>
    </form>
  )
}
