import { useState } from 'react'
import { Alert } from '../../../components/Alert'
import { Button } from '../../../components/Button'
import { Select } from '../../../components/Select'
import { FramePicker } from '../../frames/components/FramePicker'

// fetchAvailableFrames({ q, page, size }): la página de marcos que se pueden asignar.
// initialFrame: el que el beneficiario reservó en el probador, ya elegido (no figura entre los
// disponibles porque está reservado para él).
export function AssignmentForm({
  prescriptionOptions,
  fetchAvailableFrames,
  initialFrame,
  noFramesMessage,
  onSubmit,
  isPending,
  submitError,
}) {
  const [values, setValues] = useState({ prescriptionId: '', notes: '' })
  const [selectedFrames, setSelectedFrames] = useState(initialFrame ? [initialFrame] : [])
  const [fieldErrors, setFieldErrors] = useState({})

  function setField(name) {
    return (event) => setValues((current) => ({ ...current, [name]: event.target.value }))
  }

  function handleSubmit(event) {
    event.preventDefault()

    const errors = {
      prescriptionId: values.prescriptionId ? null : 'Elegí la receta que se manda a la óptica',
      frameId: selectedFrames.length > 0 ? null : 'Elegí el marco a asignar',
    }
    setFieldErrors(errors)
    if (Object.values(errors).some(Boolean)) return

    onSubmit({
      prescriptionId: Number(values.prescriptionId),
      frameId: selectedFrames[0].id,
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

      <div>
        <span className="mb-1.5 block text-sm font-semibold text-slate-700">Marco disponible</span>
        <FramePicker
          id="frameId"
          queryKey={['frames', 'AVAILABLE', 'picker']}
          fetchPage={fetchAvailableFrames}
          selected={selectedFrames}
          onChange={setSelectedFrames}
          error={fieldErrors.frameId}
          emptyMessage={noFramesMessage}
        />
      </div>

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
