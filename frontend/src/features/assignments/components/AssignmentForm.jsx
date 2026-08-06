import { useState } from 'react'
import { Alert } from '../../../components/Alert'
import { Button } from '../../../components/Button'
import { Select } from '../../../components/Select'

export function AssignmentForm({
  prescriptionOptions,
  frameOptions,
  onSubmit,
  isPending,
  submitError,
}) {
  const [values, setValues] = useState({ prescriptionId: '', frameId: '', notes: '' })
  const [fieldErrors, setFieldErrors] = useState({})

  function setField(name) {
    return (event) => setValues((current) => ({ ...current, [name]: event.target.value }))
  }

  function handleSubmit(event) {
    event.preventDefault()

    const errors = {
      prescriptionId: values.prescriptionId ? null : 'Elegí la receta que se manda a la óptica',
      frameId: values.frameId ? null : 'Elegí el marco a asignar',
    }
    setFieldErrors(errors)
    if (Object.values(errors).some(Boolean)) return

    onSubmit({
      prescriptionId: Number(values.prescriptionId),
      frameId: Number(values.frameId),
      notes: values.notes.trim() || null,
    })
  }

  return (
    <form onSubmit={handleSubmit} noValidate className="space-y-4">
      <Select
        id="prescriptionId"
        label="Receta"
        options={prescriptionOptions}
        placeholder="Elegí una receta"
        value={values.prescriptionId}
        onChange={setField('prescriptionId')}
        error={fieldErrors.prescriptionId}
      />

      <Select
        id="frameId"
        label="Marco disponible"
        options={frameOptions}
        placeholder="Elegí un marco"
        value={values.frameId}
        onChange={setField('frameId')}
        error={fieldErrors.frameId}
      />

      <div>
        <label htmlFor="notes" className="mb-1 block text-sm font-medium text-slate-700">
          Observaciones (opcionales)
        </label>
        <textarea
          id="notes"
          rows={3}
          maxLength={500}
          placeholder="Lo que la óptica tenga que saber sobre este par."
          value={values.notes}
          onChange={setField('notes')}
          className="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-slate-900 outline-none transition focus:border-sky-500 focus:ring-2 focus:ring-sky-100"
        />
      </div>

      {submitError && <Alert>{submitError}</Alert>}

      <Button type="submit" disabled={isPending}>
        {isPending ? 'Asignando…' : 'Asignar marco'}
      </Button>
    </form>
  )
}
