import { useRef, useState } from 'react'
import { Alert } from '../../../components/Alert'
import { Button } from '../../../components/Button'
import { Input } from '../../../components/Input'

// Espejan las restricciones del backend: @Digits(integer = 2, fraction = 2) y eje entre 0 y 180.
const DECIMAL_REGEX = /^[+-]?\d{1,2}(\.\d{1,2})?$/
const AXIS_REGEX = /^\d{1,3}$/

// Mismos límites que el backend: evita subir 10 MB para que los rechace del otro lado.
const ALLOWED_FILE_TYPES = ['application/pdf', 'image/jpeg', 'image/png']
const MAX_FILE_BYTES = 10 * 1024 * 1024

const EMPTY_VALUES = {
  rightSphere: '',
  rightCylinder: '',
  rightAxis: '',
  leftSphere: '',
  leftCylinder: '',
  leftAxis: '',
}

const EYES = [
  { key: 'right', label: 'Ojo derecho (OD)' },
  { key: 'left', label: 'Ojo izquierdo (OI)' },
]

function validateDecimal(raw) {
  if (!raw.trim()) return null
  return DECIMAL_REGEX.test(raw.trim()) ? null : 'Hasta 2 enteros y 2 decimales (ej. -1.25)'
}

function validateAxis(raw) {
  if (!raw.trim()) return null
  const isValid = AXIS_REGEX.test(raw.trim()) && Number(raw) <= 180
  return isValid ? null : 'El eje va de 0 a 180, sin decimales'
}

function validateFile(file) {
  if (!file) return null
  if (!ALLOWED_FILE_TYPES.includes(file.type)) return 'El archivo debe ser PDF, JPG o PNG'
  if (file.size > MAX_FILE_BYTES) return 'El archivo supera los 10 MB'
  return null
}

function EyeFields({ eyeKey, label, values, fieldErrors, setField }) {
  return (
    <fieldset className="rounded-lg border border-slate-200 p-4">
      <legend className="px-1 text-sm font-semibold text-slate-700">{label}</legend>
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <Input
          id={`${eyeKey}Sphere`}
          label="Esfera"
          type="number"
          step="0.25"
          placeholder="-1.25"
          value={values[`${eyeKey}Sphere`]}
          onChange={setField(`${eyeKey}Sphere`)}
          error={fieldErrors[`${eyeKey}Sphere`]}
        />
        <Input
          id={`${eyeKey}Cylinder`}
          label="Cilindro"
          type="number"
          step="0.25"
          placeholder="-0.50"
          value={values[`${eyeKey}Cylinder`]}
          onChange={setField(`${eyeKey}Cylinder`)}
          error={fieldErrors[`${eyeKey}Cylinder`]}
        />
        <Input
          id={`${eyeKey}Axis`}
          label="Eje"
          type="number"
          step="1"
          min="0"
          max="180"
          placeholder="90"
          value={values[`${eyeKey}Axis`]}
          onChange={setField(`${eyeKey}Axis`)}
          error={fieldErrors[`${eyeKey}Axis`]}
        />
      </div>
    </fieldset>
  )
}

export function PrescriptionForm({ onSubmit, isPending, submitError }) {
  const [values, setValues] = useState(EMPTY_VALUES)
  const [file, setFile] = useState(null)
  const [fieldErrors, setFieldErrors] = useState({})
  const [formError, setFormError] = useState(null)
  // El input de archivo es no controlado: hay que limpiarlo a mano tras guardar.
  const fileInputRef = useRef(null)

  function setField(name) {
    return (event) => setValues((current) => ({ ...current, [name]: event.target.value }))
  }

  function handleFileChange(event) {
    setFile(event.target.files[0] ?? null)
    setFieldErrors((current) => ({ ...current, file: null }))
  }

  function clearFile() {
    setFile(null)
    if (fileInputRef.current) fileInputRef.current.value = ''
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setFormError(null)

    const errors = { file: validateFile(file) }
    for (const { key } of EYES) {
      errors[`${key}Sphere`] = validateDecimal(values[`${key}Sphere`])
      errors[`${key}Cylinder`] = validateDecimal(values[`${key}Cylinder`])
      errors[`${key}Axis`] = validateAxis(values[`${key}Axis`])
      // Un cilindro sin eje no describe una graduación: el eje indica sobre qué ángulo se aplica.
      if (!errors[`${key}Axis`] && values[`${key}Cylinder`].trim() && !values[`${key}Axis`].trim()) {
        errors[`${key}Axis`] = 'Indicá el eje del cilindro'
      }
    }
    setFieldErrors(errors)
    if (Object.values(errors).some(Boolean)) return

    // El backend acepta todos los campos en null; una receta vacía no aportaría nada.
    if (Object.values(values).every((value) => !value.trim())) {
      setFormError('Cargá al menos un valor de la receta')
      return
    }

    const payload = Object.fromEntries(
      Object.entries(values).map(([name, value]) => [name, value.trim() ? Number(value) : null]),
    )

    try {
      await onSubmit(payload, file)
      setValues(EMPTY_VALUES)
      clearFile()
    } catch {
      // El mensaje del backend se muestra vía submitError; los valores quedan para reintentar.
    }
  }

  return (
    <form onSubmit={handleSubmit} noValidate className="space-y-4">
      <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
        {EYES.map(({ key, label }) => (
          <EyeFields
            key={key}
            eyeKey={key}
            label={label}
            values={values}
            fieldErrors={fieldErrors}
            setField={setField}
          />
        ))}
      </div>

      <fieldset className="rounded-lg border border-slate-200 p-4">
        <legend className="px-1 text-sm font-semibold text-slate-700">
          Receta del médico (opcional)
        </legend>
        <p className="mb-2 text-sm text-slate-500">
          Adjuntá el PDF o una foto de la receta. Se envía a la óptica junto con el marco.
        </p>
        <input
          ref={fileInputRef}
          id="prescriptionFile"
          type="file"
          accept="application/pdf,image/jpeg,image/png"
          onChange={handleFileChange}
          className="block w-full text-sm text-slate-600 file:mr-3 file:rounded-lg file:border-0 file:bg-sky-50 file:px-4 file:py-2 file:text-sm file:font-medium file:text-sky-700 hover:file:bg-sky-100"
        />
        {file && (
          <button
            type="button"
            onClick={clearFile}
            className="mt-2 text-sm font-medium text-slate-500 hover:text-slate-700 hover:underline"
          >
            Quitar «{file.name}»
          </button>
        )}
        {fieldErrors.file && <p className="mt-1 text-sm text-red-600">{fieldErrors.file}</p>}
      </fieldset>

      {formError && <Alert>{formError}</Alert>}
      {submitError && <Alert>{submitError}</Alert>}

      <Button type="submit" disabled={isPending}>
        {isPending ? 'Guardando…' : 'Registrar receta'}
      </Button>
    </form>
  )
}
