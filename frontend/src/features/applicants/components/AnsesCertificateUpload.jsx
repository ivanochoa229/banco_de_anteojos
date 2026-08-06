import { useRef, useState } from 'react'
import { Alert } from '../../../components/Alert'

// Mismo límite que el backend. Solo PDF: el barcode y la fecha se leen del archivo original de
// ANSES, una foto o un escaneo no garantizan que sean legibles.
const MAX_FILE_BYTES = 10 * 1024 * 1024

function validateFile(file) {
  if (file.type !== 'application/pdf') return 'El archivo debe ser el PDF emitido por ANSES'
  if (file.size > MAX_FILE_BYTES) return 'El archivo supera los 10 MB'
  return null
}

export function AnsesCertificateUpload({ hasCertificate, onUpload, isPending, uploadError }) {
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
        accept="application/pdf"
        onChange={handleChange}
        className="hidden"
      />
      <button
        type="button"
        onClick={() => inputRef.current?.click()}
        disabled={isPending}
        className="rounded-lg bg-sky-700 px-4 py-2.5 font-medium text-white transition hover:bg-sky-800 disabled:cursor-not-allowed disabled:opacity-60"
      >
        {isPending
          ? 'Validando el PDF…'
          : hasCertificate
            ? 'Reemplazar la certificación'
            : 'Subir la certificación'}
      </button>

      {error && (
        <div className="mt-3">
          <Alert>{error}</Alert>
        </div>
      )}
    </div>
  )
}
