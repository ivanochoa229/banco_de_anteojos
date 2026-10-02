import { useState } from 'react'
import { Alert } from '../../../components/Alert'
import { Button } from '../../../components/Button'
import { Input } from '../../../components/Input'
import { TimeSelect } from '../../../components/TimeSelect'
import { todayIsoDate } from '../../../lib/dates'

const EMPTY_VALUES = { date: '', startTime: '09:00', endTime: '13:00', slotDurationMinutes: '15' }

function toMinutes(time) {
  const [hours, minutes] = time.split(':').map(Number)
  return hours * 60 + minutes
}

function toTime(totalMinutes) {
  const pad = (value) => String(value).padStart(2, '0')
  return `${pad(Math.floor(totalMinutes / 60))}:${pad(totalMinutes % 60)}`
}

/**
 * Alta y edición de un día de atención: inicio, fin y duración de cada turno. La cantidad la
 * calcula el backend (turnos enteros que entran en el rango); acá se muestra la misma cuenta como
 * vista previa. Con turnos ya dados (`isBooked`) solo se puede mover el fin: cambiar fecha, inicio
 * o duración le cambiaría la hora a gente ya citada.
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
          endTime: initialDay.endTime.slice(0, 5),
          slotDurationMinutes: String(initialDay.slotDurationMinutes),
        }
      : EMPTY_VALUES,
  )
  const [fieldErrors, setFieldErrors] = useState({})

  function setField(name) {
    return (event) => setValues((current) => ({ ...current, [name]: event.target.value }))
  }

  function setTime(name) {
    return (time) => setValues((current) => ({ ...current, [name]: time }))
  }

  const duration = Number(values.slotDurationMinutes)
  const validDuration = Number.isInteger(duration) && duration >= 5 && duration <= 240
  const rangeMinutes = toMinutes(values.endTime) - toMinutes(values.startTime)
  const slotCount = validDuration && rangeMinutes > 0 ? Math.floor(rangeMinutes / duration) : 0
  const lastSlotEnd = toTime(toMinutes(values.startTime) + slotCount * duration)

  function handleSubmit(event) {
    event.preventDefault()

    const errors = {
      date: !values.date
        ? 'Elegí la fecha'
        : !isBooked && values.date < todayIsoDate()
          ? 'La fecha no puede estar en el pasado'
          : null,
      endTime: rangeMinutes <= 0 ? 'Tiene que ser posterior a la hora de inicio' : null,
      slotDurationMinutes: !validDuration ? 'Entre 5 y 240 minutos' : null,
    }
    if (!errors.endTime && !errors.slotDurationMinutes && slotCount === 0) {
      errors.endTime = `No entra ni un turno de ${duration} minutos`
    }
    setFieldErrors(errors)
    if (Object.values(errors).some(Boolean)) return

    onSubmit({
      date: values.date,
      startTime: values.startTime,
      endTime: values.endTime,
      slotDurationMinutes: duration,
    })
  }

  let preview = 'Completá los datos para ver cuántos turnos entran.'
  if (slotCount > 0) {
    preview = `${slotCount} ${slotCount === 1 ? 'turno' : 'turnos'} de ${duration} min, de ${
      values.startTime
    } a ${lastSlotEnd}.`
    // Si no divide exacto, que quede claro que el último turno termina antes del fin cargado.
    if (lastSlotEnd !== values.endTime) {
      preview += ` De ${lastSlotEnd} a ${values.endTime} no entra otro turno.`
    }
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
        <TimeSelect
          id="startTime"
          label="Hora de inicio"
          disabled={isBooked}
          value={values.startTime}
          onChange={setTime('startTime')}
        />
        <TimeSelect
          id="endTime"
          label="Hora de fin"
          value={values.endTime}
          onChange={setTime('endTime')}
          error={fieldErrors.endTime}
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
      </div>

      <p className="text-sm text-slate-500">
        {preview}
        {isBooked &&
          ' Este día ya tiene turnos dados: solo se puede cambiar la hora de fin (sin dejar afuera a nadie).'}
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
