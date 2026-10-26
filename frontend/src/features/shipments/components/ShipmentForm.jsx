import { useState } from 'react'
import { Alert } from '../../../components/Alert'
import { Button } from '../../../components/Button'
import { Input } from '../../../components/Input'
import { FramePicker } from '../../frames/components/FramePicker'

/**
 * El paquete suele llevar varios marcos: se eligen con el buscador del inventario y la selección
 * se mantiene aunque se cambie la búsqueda o la página.
 *
 * fetchFrames({ q, page, size }): la página del inventario de donde se eligen.
 */
export function ShipmentForm({ fetchFrames, noFramesMessage, onSubmit, isPending, submitError }) {
  const [values, setValues] = useState({ originBranch: '', destinationBranch: '', notes: '' })
  const [selectedFrames, setSelectedFrames] = useState([])
  const [fieldErrors, setFieldErrors] = useState({})

  function setField(name) {
    return (event) => setValues((current) => ({ ...current, [name]: event.target.value }))
  }

  function handleSubmit(event) {
    event.preventDefault()

    const origin = values.originBranch.trim()
    const destination = values.destinationBranch.trim()
    const errors = {
      originBranch: !origin ? 'Indicá desde qué sucursal sale el paquete' : null,
      destinationBranch: !destination
        ? 'Indicá a qué sucursal va el paquete'
        : destination.toLowerCase() === origin.toLowerCase()
          ? 'El destino tiene que ser distinto del origen'
          : null,
      frames: selectedFrames.length === 0 ? 'El paquete tiene que llevar al menos un marco' : null,
    }
    setFieldErrors(errors)
    if (Object.values(errors).some(Boolean)) return

    onSubmit({
      originBranch: origin,
      destinationBranch: destination,
      frameIds: selectedFrames.map((frame) => frame.id),
      notes: values.notes.trim() || null,
    })
  }

  return (
    <form onSubmit={handleSubmit} noValidate className="space-y-4">
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <Input
          id="originBranch"
          label="Sucursal de origen"
          maxLength={120}
          placeholder="Sede Chacabuco 27"
          value={values.originBranch}
          onChange={setField('originBranch')}
          error={fieldErrors.originBranch}
        />
        <Input
          id="destinationBranch"
          label="Sucursal de destino"
          maxLength={120}
          placeholder="Delegación Concepción"
          value={values.destinationBranch}
          onChange={setField('destinationBranch')}
          error={fieldErrors.destinationBranch}
        />
      </div>

      <div>
        <div className="mb-1 flex items-end justify-between gap-4">
          <span className="block text-sm font-medium text-slate-700">Marcos del paquete</span>
          <span className="text-sm text-slate-500">
            {selectedFrames.length} seleccionado{selectedFrames.length === 1 ? '' : 's'}
          </span>
        </div>

        <FramePicker
          id="frames"
          queryKey={['frames', 'picker']}
          fetchPage={fetchFrames}
          selected={selectedFrames}
          onChange={setSelectedFrames}
          multiple
          showStatus
          error={fieldErrors.frames}
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
          placeholder="Van con las recetas adjuntas en el sobre."
          value={values.notes}
          onChange={setField('notes')}
          className="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-slate-900 outline-none transition focus:border-sky-500 focus:ring-2 focus:ring-sky-100"
        />
      </div>

      {submitError && <Alert>{submitError}</Alert>}

      <Button type="submit" disabled={isPending}>
        {isPending ? 'Armando el paquete…' : 'Armar el paquete'}
      </Button>
    </form>
  )
}
