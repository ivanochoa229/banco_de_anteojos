import { useState } from 'react'
import { Button } from '../../../components/Button'
import { Input } from '../../../components/Input'

// Acción inline de venta: se abre en el lugar de la tarjeta, como el despacho de envíos. Cada
// anteojo es único, así que no hay cantidad: confirmar lo marca como vendido.
export function SaleForm({ productId, onSell, isPending, submitError, onCancel }) {
  const [buyerName, setBuyerName] = useState('')

  function handleSubmit(event) {
    event.preventDefault()
    onSell({ buyerName: buyerName.trim() || null })
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-2">
      <Input
        id={`buyer-${productId}`}
        label="Comprador (opcional)"
        value={buyerName}
        onChange={(event) => setBuyerName(event.target.value)}
      />
      <div className="flex items-center gap-2">
        <Button type="submit" disabled={isPending} className="px-3 py-2 text-sm">
          {isPending ? 'Vendiendo…' : 'Confirmar venta'}
        </Button>
        <button
          type="button"
          onClick={onCancel}
          className="px-2 py-2 text-sm font-medium text-slate-500 hover:underline"
        >
          Volver
        </button>
      </div>
      {submitError && <p className="text-sm text-red-600">{submitError}</p>}
    </form>
  )
}
