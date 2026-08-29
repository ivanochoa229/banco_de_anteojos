import { useRef, useState } from 'react'
import { Alert } from '../../../components/Alert'

const MAX_FILE_BYTES = 5 * 1024 * 1024
const ACCEPTED_TYPES = ['image/jpeg', 'image/png', 'image/webp']

function validateFile(file) {
  if (!ACCEPTED_TYPES.includes(file.type)) return 'La imagen debe ser JPG, PNG o WEBP'
  if (file.size > MAX_FILE_BYTES) return 'La imagen supera los 5 MB'
  return null
}

export function ProductImageUpload({ hasImage, onUpload, isPending, uploadError }) {
  const inputRef = useRef(null)
  const [validationError, setValidationError] = useState(null)

  function handleChange(event) {
    const file = event.target.files[0]
    // Se limpia el input para que elegir el mismo archivo otra vez vuelva a disparar el evento.
    event.target.value = ''
    if (!file) return

    const error = validateFile(file)
    setValidationError(error)
    if (!error) onUpload(file)
  }

  const error = validationError ?? uploadError

  return (
    <div>
      <input
        ref={inputRef}
        type="file"
        accept={ACCEPTED_TYPES.join(',')}
        onChange={handleChange}
        className="hidden"
      />
      <button
        type="button"
        onClick={() => inputRef.current?.click()}
        disabled={isPending}
        className="rounded-lg bg-sky-700 px-4 py-2.5 font-medium text-white transition hover:bg-sky-800 disabled:cursor-not-allowed disabled:opacity-60"
      >
        {isPending ? 'Subiendo…' : hasImage ? 'Reemplazar la foto' : 'Subir una foto'}
      </button>

      {error && (
        <div className="mt-3">
          <Alert>{error}</Alert>
        </div>
      )}
    </div>
  )
}
