import { useState } from 'react'
import { Alert } from '../../../components/Alert'
import { Button } from '../../../components/Button'
import { Input } from '../../../components/Input'
import { FrameStatusBadge } from '../../frames/components/FrameStatusBadge'

/**
 * Los marcos se eligen con checkboxes y no con un multi-select nativo: el paquete suele llevar
 * varios y el select múltiple del navegador se opera con ctrl+click, que es justo lo que no se
 * le puede pedir al personal de la fundación (RNF-05).
 */
export function ShipmentForm({ frameOptions, onSubmit, isPending, submitError }) {
  const [values, setValues] = useState({ originBranch: '', destinationBranch: '', notes: '' })
  const [selectedIds, setSelectedIds] = useState([])
  const [search, setSearch] = useState('')
  const [fieldErrors, setFieldErrors] = useState({})

  function setField(name) {
    return (event) => setValues((current) => ({ ...current, [name]: event.target.value }))
  }

  function toggleFrame(frameId) {
    setSelectedIds((current) =>
      current.includes(frameId)
        ? current.filter((id) => id !== frameId)
        : [...current, frameId],
    )
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
      frames: selectedIds.length === 0 ? 'El paquete tiene que llevar al menos un marco' : null,
    }
    setFieldErrors(errors)
    if (Object.values(errors).some(Boolean)) return

    onSubmit({
      originBranch: origin,
      destinationBranch: destination,
      frameIds: selectedIds,
      notes: values.notes.trim() || null,
    })
  }

  const term = search.trim().toLowerCase()
  const visibleFrames = term
    ? frameOptions.filter((frame) => frame.label.toLowerCase().includes(term))
    : frameOptions

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
            {selectedIds.length} seleccionado{selectedIds.length === 1 ? '' : 's'}
          </span>
        </div>

        <input
          type="search"
          aria-label="Buscar marco por precinto"
          placeholder="Buscar por precinto…"
          value={search}
          onChange={(event) => setSearch(event.target.value)}
          className="mb-2 w-full rounded-lg border border-slate-300 px-3 py-2 text-slate-900 outline-none transition focus:border-sky-500 focus:ring-2 focus:ring-sky-100"
        />

        <div
          className={`max-h-64 overflow-y-auto rounded-lg border ${
            fieldErrors.frames ? 'border-red-400' : 'border-slate-300'
          }`}
        >
          {visibleFrames.length === 0 ? (
            <p className="px-3 py-4 text-sm text-slate-500">
              Ningún marco coincide con la búsqueda.
            </p>
          ) : (
            <ul className="divide-y divide-slate-100">
              {visibleFrames.map((frame) => (
                <li key={frame.value}>
                  <label className="flex cursor-pointer items-center gap-3 px-3 py-2.5 hover:bg-slate-50">
                    <input
                      type="checkbox"
                      checked={selectedIds.includes(frame.value)}
                      onChange={() => toggleFrame(frame.value)}
                      className="h-4 w-4 rounded border-slate-300 text-sky-700 focus:ring-sky-500"
                    />
                    <span className="flex-1 text-sm text-slate-700">{frame.label}</span>
                    <FrameStatusBadge status={frame.status} />
                  </label>
                </li>
              ))}
            </ul>
          )}
        </div>
        {fieldErrors.frames && <p className="mt-1 text-sm text-red-600">{fieldErrors.frames}</p>}
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
