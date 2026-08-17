import { useState } from 'react'
import { Button } from '../../../components/Button'
import { Input } from '../../../components/Input'
import { isPending as isShipmentPending } from '../labels'

/**
 * Despachar y cancelar solo aparecen mientras el paquete está pendiente: después el estado lo
 * mueve el webhook del carrier y cualquier acción nuestra se come un 409.
 *
 * El despacho pide el número de seguimiento en el momento porque es lo que dispara el registro
 * en 17TRACK: sin número no hay nada que seguir.
 */
export function ShipmentActions({ shipment, onDispatch, onCancel, isPending }) {
  const [mode, setMode] = useState(null)
  const [trackingNumber, setTrackingNumber] = useState('')
  const [reason, setReason] = useState('')
  const [trackingError, setTrackingError] = useState(null)

  if (!isShipmentPending(shipment)) return <span className="text-sm text-slate-400">—</span>

  function handleDispatch(event) {
    event.preventDefault()
    const value = trackingNumber.trim()
    // Mismo rango que acepta 17TRACK: avisar acá evita un viaje al backend para el mismo error.
    if (value.length < 5 || value.length > 50) {
      setTrackingError('El número debe tener entre 5 y 50 caracteres')
      return
    }
    setTrackingError(null)
    onDispatch(value)
  }

  if (mode === 'dispatch') {
    return (
      <form onSubmit={handleDispatch} className="flex flex-wrap items-end gap-2">
        <div className="w-56">
          <Input
            id={`tracking-${shipment.id}`}
            label="Número de seguimiento"
            maxLength={50}
            placeholder="VC123456789"
            value={trackingNumber}
            onChange={(event) => setTrackingNumber(event.target.value)}
            error={trackingError}
          />
        </div>
        <Button type="submit" disabled={isPending} className="px-3 py-2 text-sm">
          {isPending ? 'Despachando…' : 'Confirmar'}
        </Button>
        <button
          type="button"
          onClick={() => setMode(null)}
          className="px-2 py-2 text-sm font-medium text-slate-500 hover:underline"
        >
          Volver
        </button>
      </form>
    )
  }

  if (mode === 'cancel') {
    return (
      <form
        onSubmit={(event) => {
          event.preventDefault()
          onCancel(reason.trim())
        }}
        className="flex flex-wrap items-end gap-2"
      >
        <div className="w-56">
          <Input
            id={`reason-${shipment.id}`}
            label="Motivo (opcional)"
            maxLength={255}
            placeholder="Se rearma con otros marcos"
            value={reason}
            onChange={(event) => setReason(event.target.value)}
          />
        </div>
        <Button
          type="submit"
          disabled={isPending}
          className="bg-red-700 px-3 py-2 text-sm hover:bg-red-800"
        >
          {isPending ? 'Cancelando…' : 'Confirmar'}
        </Button>
        <button
          type="button"
          onClick={() => setMode(null)}
          className="px-2 py-2 text-sm font-medium text-slate-500 hover:underline"
        >
          Volver
        </button>
      </form>
    )
  }

  return (
    <div className="flex items-center gap-3">
      <Button onClick={() => setMode('dispatch')} disabled={isPending} className="px-3 py-2 text-sm">
        Despachar
      </Button>
      <button
        type="button"
        onClick={() => setMode('cancel')}
        disabled={isPending}
        className="text-sm font-medium text-slate-500 hover:underline disabled:opacity-60"
      >
        Cancelar
      </button>
    </div>
  )
}
