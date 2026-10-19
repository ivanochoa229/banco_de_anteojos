import { useState } from 'react'
import { Alert } from '../../../components/Alert'
import { Button } from '../../../components/Button'
import { Select } from '../../../components/Select'
import { appointmentDayLabel } from '../labels'

/**
 * Se elige el día de atención, no la hora: la sede solo atiende los días que habilita el personal
 * y el backend asigna la primera franja libre de ese día. `days` ya viene filtrado a los que tienen
 * lugar.
 */
export function AppointmentForm({ days, isLoadingDays, assignmentOptions, onSubmit, isPending, submitError }) {
  const [values, setValues] = useState({ appointmentDayId: '', assignmentId: '', notes: '' })
  const [fieldErrors, setFieldErrors] = useState({})

  function setField(name) {
    return (event) => setValues((current) => ({ ...current, [name]: event.target.value }))
  }

  function handleSubmit(event) {
    event.preventDefault()

    const errors = {
      appointmentDayId: !values.appointmentDayId ? 'Elegí el día del turno' : null,
    }
    setFieldErrors(errors)
    if (Object.values(errors).some(Boolean)) return

    onSubmit({
      appointmentDayId: Number(values.appointmentDayId),
      assignmentId: values.assignmentId ? Number(values.assignmentId) : null,
      notes: values.notes.trim() || null,
    })
  }

  if (isLoadingDays) {
    return <p className="text-sm text-slate-500">Cargando días de atención…</p>
  }

  if (days.length === 0) {
    return (
      <p className="rounded-lg border border-dashed border-slate-300 px-4 py-6 text-center text-sm text-slate-500">
        Por ahora no hay días de atención con turnos libres. Volvé a consultar en unos días.
      </p>
    )
  }

  return (
    <form onSubmit={handleSubmit} noValidate className="space-y-4">
      <div>
        <Select
          id="appointmentDayId"
          label="Día de atención"
          options={days.map((day) => ({ value: day.id, label: appointmentDayLabel(day) }))}
          placeholder="Elegí un día"
          value={values.appointmentDayId}
          onChange={setField('appointmentDayId')}
          error={fieldErrors.appointmentDayId}
        />
        <p className="mt-1.5 text-xs text-slate-500">
          La hora se asigna sola: el primer turno libre de ese día.
        </p>
      </div>

      {assignmentOptions.length > 0 && (
        <Select
          id="assignmentId"
          label="Retiro de anteojos (opcional)"
          options={assignmentOptions}
          placeholder="Turno de atención general"
          value={values.assignmentId}
          onChange={setField('assignmentId')}
        />
      )}

      <div>
        <label htmlFor="notes" className="mb-1 block text-sm font-medium text-slate-700">
          Observaciones (opcionales)
        </label>
        <textarea
          id="notes"
          rows={3}
          maxLength={500}
          placeholder="Que traiga la receta original."
          value={values.notes}
          onChange={setField('notes')}
          className="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-slate-900 outline-none transition focus:border-sky-500 focus:ring-2 focus:ring-sky-100"
        />
      </div>

      {submitError && <Alert>{submitError}</Alert>}

      <Button type="submit" disabled={isPending}>
        {isPending ? 'Agendando…' : 'Agendar turno'}
      </Button>
    </form>
  )
}
