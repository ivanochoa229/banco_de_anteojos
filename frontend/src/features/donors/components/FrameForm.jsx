import { useState } from 'react'
import { Alert } from '../../../components/Alert'
import { Button } from '../../../components/Button'
import { Input } from '../../../components/Input'
import { Select } from '../../../components/Select'
import { FRAME_MATERIAL_OPTIONS, FRAME_TYPE_OPTIONS } from '../../frames/labels'

// Mismos rangos que el backend: una medida imposible se corta acá y no viaja.
const MEASUREMENTS = [
  { name: 'lensWidthMm', label: 'Calibre (mm)', min: 20, max: 80, placeholder: '52' },
  { name: 'bridgeWidthMm', label: 'Puente (mm)', min: 10, max: 30, placeholder: '18' },
  { name: 'templeLengthMm', label: 'Patilla (mm)', min: 100, max: 160, placeholder: '140' },
]

const EMPTY_VALUES = {
  sealCode: '',
  frameType: 'FULL_RIM',
  material: 'ACETATE',
  lensWidthMm: '',
  bridgeWidthMm: '',
  templeLengthMm: '',
}

function toFormValues(frame) {
  return {
    sealCode: frame.sealCode ?? '',
    frameType: frame.frameType ?? 'FULL_RIM',
    material: frame.material ?? 'ACETATE',
    lensWidthMm: frame.lensWidthMm?.toString() ?? '',
    bridgeWidthMm: frame.bridgeWidthMm?.toString() ?? '',
    templeLengthMm: frame.templeLengthMm?.toString() ?? '',
  }
}

function validateMeasurement(raw, { label, min, max }) {
  if (!raw.trim()) return null
  const value = Number(raw)
  if (!Number.isInteger(value) || value < min || value > max) {
    return `${label} va de ${min} a ${max}, sin decimales`
  }
  return null
}

// initialValues: un marco existente para editar. Sin él, arranca vacío para dar de alta.
export function FrameForm({ initialValues, submitLabel, onSubmit, isPending, submitError }) {
  const [values, setValues] = useState(initialValues ? toFormValues(initialValues) : EMPTY_VALUES)
  const [fieldErrors, setFieldErrors] = useState({})

  function setField(name) {
    return (event) => setValues((current) => ({ ...current, [name]: event.target.value }))
  }

  async function handleSubmit(event) {
    event.preventDefault()

    const errors = {}
    if (!values.sealCode.trim()) errors.sealCode = 'Ingresá el número de precinto'
    for (const measurement of MEASUREMENTS) {
      errors[measurement.name] = validateMeasurement(values[measurement.name], measurement)
    }
    setFieldErrors(errors)
    if (Object.values(errors).some(Boolean)) return

    const measurements = Object.fromEntries(
      MEASUREMENTS.map(({ name }) => [name, values[name].trim() ? Number(values[name]) : null]),
    )

    try {
      // El backend guarda el precinto en mayúsculas: lo mandamos así para que coincida
      // con lo que después se ve en el inventario.
      await onSubmit({
        sealCode: values.sealCode.trim().toUpperCase(),
        frameType: values.frameType,
        material: values.material,
        ...measurements,
      })
      // En edición la página navega al guardar; limpiar el form ahí encima no aporta nada.
      if (!initialValues) setValues(EMPTY_VALUES)
    } catch {
      // El mensaje del backend se muestra vía submitError; los valores quedan para corregir
      // (típicamente un precinto repetido).
    }
  }

  return (
    <form onSubmit={handleSubmit} noValidate className="space-y-4">
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <Input
          id="sealCode"
          label="Número de precinto"
          placeholder="BA-0001"
          value={values.sealCode}
          onChange={setField('sealCode')}
          error={fieldErrors.sealCode}
        />
        <Select
          id="frameType"
          label="Tipo de armazón"
          options={FRAME_TYPE_OPTIONS}
          value={values.frameType}
          onChange={setField('frameType')}
        />
        <Select
          id="material"
          label="Material"
          options={FRAME_MATERIAL_OPTIONS}
          value={values.material}
          onChange={setField('material')}
        />
      </div>

      <fieldset className="rounded-lg border border-slate-200 p-4">
        <legend className="px-1 text-sm font-semibold text-slate-700">Medidas (opcionales)</legend>
        <p className="mb-3 text-sm text-slate-500">
          Vienen grabadas en la patilla del marco. Si no se leen, dejalas vacías.
        </p>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
          {MEASUREMENTS.map(({ name, label, min, max, placeholder }) => (
            <Input
              key={name}
              id={name}
              label={label}
              type="number"
              step="1"
              min={min}
              max={max}
              placeholder={placeholder}
              value={values[name]}
              onChange={setField(name)}
              error={fieldErrors[name]}
            />
          ))}
        </div>
      </fieldset>

      {submitError && <Alert>{submitError}</Alert>}

      <Button type="submit" disabled={isPending}>
        {isPending ? 'Guardando…' : (submitLabel ?? 'Agregar marco')}
      </Button>
    </form>
  )
}
