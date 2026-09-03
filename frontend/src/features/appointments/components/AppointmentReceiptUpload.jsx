import { useRef, useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { appointmentsApi } from '../../../api/appointmentsApi'
import { Alert } from '../../../components/Alert'

// Mismos límites que el backend: evita subir 10 MB para que los rechace del otro lado.
const ALLOWED_FILE_TYPES = ['application/pdf', 'image/jpeg', 'image/png']
const MAX_FILE_BYTES = 10 * 1024 * 1024

// TODO: completar con los datos reales de la cuenta de la fundación antes de deployar.
const BANK_ACCOUNT = { alias: 'BANCO.ANTEOJOS.HF', cbu: '0000000000000000000000', titular: 'Fundación Hacer Futuro' }

function validateFile(file) {
  if (!ALLOWED_FILE_TYPES.includes(file.type)) return 'El archivo debe ser PDF, JPG o PNG'
  if (file.size > MAX_FILE_BYTES) return 'El archivo supera los 10 MB'
  return null
}

/**
 * El turno queda pendiente hasta subir el comprobante de la transferencia del bono contribución
 * (evita que agenden turnos sin intención de venir). El monto no lo valida el sistema: lo revisa
 * un operador después, como pasa hoy con la receta o el certificado ANSES.
 */
export function AppointmentReceiptUpload({ appointmentId }) {
  const queryClient = useQueryClient()
  const inputRef = useRef(null)
  const [validationError, setValidationError] = useState(null)

  const uploadMutation = useMutation({
    mutationFn: (file) => appointmentsApi.uploadReceipt(appointmentId, file),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['me', 'appointments'] }),
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
    <div className="mt-2 rounded-lg border border-amber-200 bg-amber-50 p-3 text-sm">
      <p className="font-medium text-amber-800">Falta confirmar con el comprobante del bono contribución</p>
      <p className="mt-1 text-amber-700">
        Transferí a alias <span className="font-semibold">{BANK_ACCOUNT.alias}</span> (CBU{' '}
        {BANK_ACCOUNT.cbu}, {BANK_ACCOUNT.titular}) y subí el comprobante para confirmar el turno.
      </p>

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
        className="mt-2 rounded-lg bg-amber-700 px-3 py-1.5 text-sm font-medium text-white transition hover:bg-amber-800 disabled:cursor-not-allowed disabled:opacity-60"
      >
        {uploadMutation.isPending ? 'Subiendo…' : 'Subir comprobante'}
      </button>

      {error && (
        <div className="mt-2">
          <Alert>{error}</Alert>
        </div>
      )}
    </div>
  )
}
