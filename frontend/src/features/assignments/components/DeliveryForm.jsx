import { useState } from 'react'
import { Alert } from '../../../components/Alert'
import { Button } from '../../../components/Button'

// Mismo límite que el backend. Formatos que sacan las cámaras de los celulares.
const MAX_FILE_BYTES = 10 * 1024 * 1024
const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp']

function validatePhoto(file) {
  if (!ALLOWED_TYPES.includes(file.type)) return 'La foto debe ser JPG, PNG o WEBP'
  if (file.size > MAX_FILE_BYTES) return 'La foto supera los 10 MB'
  return null
}

/**
 * Confirmación de la entrega con la foto opcional del beneficiario con sus anteojos. La foto se
 * le manda al donante solo si el beneficiario lo autorizó: sin la casilla queda guardada en la
 * asignación pero no sale de la fundación.
 */
export function DeliveryForm({ assignmentId, onConfirm, onBack, isPending }) {
  const [photo, setPhoto] = useState(null)
  const [photoConsent, setPhotoConsent] = useState(false)
  const [photoError, setPhotoError] = useState(null)

  function handlePhotoChange(event) {
    const file = event.target.files[0] ?? null
    const error = file ? validatePhoto(file) : null
    setPhotoError(error)
    setPhoto(error ? null : file)
    if (!file || error) setPhotoConsent(false)
  }

  return (
    <form
      onSubmit={(event) => {
        event.preventDefault()
        onConfirm({ photo, photoConsent: Boolean(photo) && photoConsent })
      }}
      className="max-w-md space-y-3 rounded-xl border border-slate-200 bg-slate-50 p-4"
    >
      <div>
        <label
          htmlFor={`delivery-photo-${assignmentId}`}
          className="mb-1.5 block text-sm font-semibold text-slate-700"
        >
          Foto del beneficiario con sus anteojos (opcional)
        </label>
        <input
          id={`delivery-photo-${assignmentId}`}
          type="file"
          accept={ALLOWED_TYPES.join(',')}
          onChange={handlePhotoChange}
          className="block w-full text-sm text-slate-600 file:mr-3 file:rounded-lg file:border-0 file:bg-white file:px-3 file:py-2 file:text-sm file:font-semibold file:text-slate-700 file:shadow-xs hover:file:bg-slate-100"
        />
        {photoError && (
          <div className="mt-2">
            <Alert>{photoError}</Alert>
          </div>
        )}
      </div>

      <label className={`flex items-start gap-2 text-sm ${photo ? 'text-slate-700' : 'text-slate-400'}`}>
        <input
          type="checkbox"
          checked={photoConsent}
          disabled={!photo}
          onChange={(event) => setPhotoConsent(event.target.checked)}
          className="mt-0.5 h-4 w-4 accent-orange-600"
        />
        <span>
          El beneficiario autorizó compartir la foto con el donante del marco.
          <span className="block text-xs text-slate-500">
            Sin esta autorización la foto queda guardada en la asignación pero no se le envía.
          </span>
        </span>
      </label>

      <p className="text-xs text-slate-500">
        Al confirmar se le avisa por mail al donante que su marco fue entregado
        {photo && photoConsent ? ', con la foto adjunta' : ''}.
      </p>

      <div className="flex items-center gap-3">
        <Button type="submit" disabled={isPending} className="px-3 py-2 text-sm">
          {isPending ? 'Registrando…' : 'Confirmar entrega'}
        </Button>
        <button
          type="button"
          onClick={onBack}
          className="px-2 py-2 text-sm font-medium text-slate-500 hover:underline"
        >
          Volver
        </button>
      </div>
    </form>
  )
}
