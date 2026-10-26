import { useRef, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { applicantsApi } from '../../../api/applicantsApi'
import { Alert } from '../../../components/Alert'
import { formatDateTime } from '../../../lib/dates'
import { PrescriptionFileLink } from './PrescriptionFileLink'

// Mismos límites que el backend: evita subir 10 MB para que los rechace del otro lado.
const ALLOWED_FILE_TYPES = ['application/pdf', 'image/jpeg', 'image/png']
const MAX_FILE_BYTES = 10 * 1024 * 1024

function validateFile(file) {
  if (!ALLOWED_FILE_TYPES.includes(file.type)) return 'La receta debe ser un PDF o una foto (JPG o PNG)'
  if (file.size > MAX_FILE_BYTES) return 'El archivo supera los 10 MB'
  return null
}

/**
 * Receta del médico subida por el propio beneficiario. No se tipea la graduación: la óptica la
 * lee del archivo, y el operador la completa si la necesita.
 */
export function MyPrescriptionsSection() {
  const queryClient = useQueryClient()
  const inputRef = useRef(null)
  const [validationError, setValidationError] = useState(null)

  const prescriptionsQuery = useQuery({
    queryKey: ['me', 'prescriptions'],
    queryFn: applicantsApi.listMyPrescriptions,
  })

  const uploadMutation = useMutation({
    mutationFn: applicantsApi.uploadMyPrescription,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['me', 'prescriptions'] }),
  })

  function handleChange(event) {
    const file = event.target.files[0]
    // Se limpia el input para que elegir el mismo archivo otra vez vuelva a disparar el evento.
    event.target.value = ''
    if (!file) return

    const error = validateFile(file)
    setValidationError(error)
    if (!error) uploadMutation.mutate(file)
  }

  // Del más nuevo al más viejo: la receta vigente es la última que trajo.
  const prescriptions = [...(prescriptionsQuery.data ?? [])].sort((a, b) =>
    b.createdAt.localeCompare(a.createdAt),
  )
  const error = validationError ?? uploadMutation.error?.message

  return (
    <section>
      <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-500">Receta médica</h3>
      <p className="mt-1 text-sm text-slate-500">
        Subí la receta del oftalmólogo como PDF o una foto donde se lea bien la graduación. Es la
        que se manda a la óptica junto con tu marco.
      </p>
      <div className="mt-3 rounded-lg border border-slate-200 bg-white p-6">
        {prescriptionsQuery.isError && <Alert>{prescriptionsQuery.error.message}</Alert>}
        {prescriptions.length > 0 && (
          <ul className="mb-5 divide-y divide-slate-100 text-sm">
            {prescriptions.map((prescription) => (
              <li key={prescription.id} className="flex items-center justify-between gap-4 py-2">
                <PrescriptionFileLink
                  fetchFile={() => applicantsApi.getMyPrescriptionFile(prescription.id)}
                  fileName={prescription.fileOriginalName ?? 'Receta cargada en la sede'}
                />
                <span className="text-slate-500">{formatDateTime(prescription.createdAt)}</span>
              </li>
            ))}
          </ul>
        )}
        {uploadMutation.isSuccess && (
          <div
            role="status"
            className="mb-4 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800"
          >
            Receta cargada.
          </div>
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
          className="rounded-lg bg-sky-700 px-4 py-2.5 font-medium text-white transition hover:bg-sky-800 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {uploadMutation.isPending
            ? 'Subiendo…'
            : prescriptions.length > 0
              ? 'Subir otra receta'
              : 'Subir la receta'}
        </button>

        {error && (
          <div className="mt-3">
            <Alert>{error}</Alert>
          </div>
        )}
      </div>
    </section>
  )
}
