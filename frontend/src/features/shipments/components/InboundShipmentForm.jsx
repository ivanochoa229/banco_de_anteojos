import { useState } from 'react'
import { Alert } from '../../../components/Alert'
import { Button } from '../../../components/Button'
import { Input } from '../../../components/Input'

/**
 * Carga de la guía que el donante informa después de despachar por Vía Cargo. Se pide la
 * "Guía No." del comprobante, no el remito: es el número que reconoce el seguimiento.
 */
export function InboundShipmentForm({ onSubmit, isPending, submitError }) {
  const [trackingNumber, setTrackingNumber] = useState('')
  const [notes, setNotes] = useState('')
  const [trackingError, setTrackingError] = useState(null)

  async function handleSubmit(event) {
    event.preventDefault()
    const value = trackingNumber.trim()
    // Mismo rango que acepta 17TRACK: avisar acá evita un viaje al backend para el mismo error.
    if (value.length < 5 || value.length > 50) {
      setTrackingError('La guía debe tener entre 5 y 50 caracteres')
      return
    }
    setTrackingError(null)
    try {
      await onSubmit({ trackingNumber: value, notes: notes.trim() || null })
      setTrackingNumber('')
      setNotes('')
    } catch {
      // El error ya lo muestra submitError.
    }
  }

  return (
    <form onSubmit={handleSubmit} noValidate className="max-w-xl space-y-4">
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <Input
          id="trackingNumber"
          label="Guía No. (del comprobante de Vía Cargo)"
          maxLength={50}
          inputMode="numeric"
          placeholder="999038143994"
          value={trackingNumber}
          onChange={(event) => setTrackingNumber(event.target.value)}
          error={trackingError}
        />
        <Input
          id="notes"
          label="Observaciones (opcional)"
          maxLength={500}
          placeholder="Dice que manda 4 marcos"
          value={notes}
          onChange={(event) => setNotes(event.target.value)}
        />
      </div>

      {submitError && <Alert>{submitError}</Alert>}

      <Button type="submit" disabled={isPending}>
        {isPending ? 'Registrando…' : 'Registrar donación en camino'}
      </Button>
    </form>
  )
}
