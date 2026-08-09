import { useState } from 'react'
import { Alert } from '../../../components/Alert'
import { Button } from '../../../components/Button'
import { Input } from '../../../components/Input'
import { Select } from '../../../components/Select'

export function AppointmentForm({ assignmentOptions, onSubmit, isPending, submitError }) {
  const [values, setValues] = useState({ scheduledAt: '', assignmentId: '', notes: '' })
  const [fieldErrors, setFieldErrors] = useState({})

  function setField(name) {
    return (event) => setValues((current) => ({ ...current, [name]: event.target.value }))
  }

  function handleSubmit(event) {
    event.preventDefault()

    const errors = {
      scheduledAt: !values.scheduledAt
        ? 'Elegí la fecha y hora del turno'
        : new Date(values.scheduledAt) <= new Date()
          ? 'El turno debe ser en el futuro'
          : null,
    }
    setFieldErrors(errors)
    if (Object.values(errors).some(Boolean)) return

    onSubmit({
      scheduledAt: values.scheduledAt,
      assignmentId: values.assignmentId ? Number(values.assignmentId) : null,
      notes: values.notes.trim() || null,
    })
  }

  return (
    <form onSubmit={handleSubmit} noValidate className="space-y-4">
      <Input
        id="scheduledAt"
        label="Fecha y hora"
        type="datetime-local"
        value={values.scheduledAt}
        onChange={setField('scheduledAt')}
        error={fieldErrors.scheduledAt}
      />

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
