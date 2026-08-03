import { useRef, useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { applicantsApi } from '../../../api/applicantsApi'
import { PrescriptionFileLink } from './PrescriptionFileLink'

// Mismos límites que el backend: evita subir 10 MB para que los rechace del otro lado.
const ALLOWED_FILE_TYPES = ['application/pdf', 'image/jpeg', 'image/png']
const MAX_FILE_BYTES = 10 * 1024 * 1024

function validateFile(file) {
  if (!ALLOWED_FILE_TYPES.includes(file.type)) return 'El archivo debe ser PDF, JPG o PNG'
  if (file.size > MAX_FILE_BYTES) return 'El archivo supera los 10 MB'
  return null
}

/**
 * Adjuntar el archivo desde el historial cubre los tres casos que el alta no resuelve: subirlo
 * más tarde, reintentar cuando la subida falló y reemplazar un escaneo ilegible.
 */
export function PrescriptionFileCell({ applicantId, prescription }) {
  const queryClient = useQueryClient()
  const inputRef = useRef(null)
  const [validationError, setValidationError] = useState(null)

  const uploadMutation = useMutation({
    mutationFn: (file) => applicantsApi.uploadPrescriptionFile(applicantId, prescription.id, file),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['applicants', applicantId, 'prescriptions'] })
    },
  })

  function handleChange(event) {
    const file = event.target.files[0]
    event.target.value = ''
    if (!file) return

    const error = validateFile(file)
    setValidationError(error)
    if (!error) uploadMutation.mutate(file)
  }

  const error = validationError ?? uploadMutation.error?.message

  return (
    <div>
      {prescription.fileOriginalName ? (
        <PrescriptionFileLink
          applicantId={applicantId}
          prescriptionId={prescription.id}
          fileName={prescription.fileOriginalName}
        />
      ) : (
        <span className="text-slate-400">Sin archivo</span>
      )}

      <input
        ref={inputRef}
        type="file"
        accept="application/pdf,image/jpeg,image/png"
        onChange={handleChange}
        className="hidden"
      />
      <button
        type="button"
        onClick={() => inputRef.current?.click()}
        disabled={uploadMutation.isPending}
        className="mt-1 block text-sm font-medium text-slate-500 hover:text-slate-700 hover:underline disabled:text-slate-300 disabled:no-underline"
      >
        {uploadMutation.isPending
          ? 'Subiendo…'
          : prescription.fileOriginalName
            ? 'Reemplazar'
            : 'Adjuntar'}
      </button>

      {error && <p className="mt-1 text-sm text-red-600">{error}</p>}
    </div>
  )
}
