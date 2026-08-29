import { useState } from 'react'
import { Button } from '../../../components/Button'
import { Input } from '../../../components/Input'

// Acción inline de venta: se abre en el lugar de la fila, como el despacho de envíos.
export function SaleForm({ productId, maxQuantity, onSell, isPending, submitError, onCancel }) {
  const [quantity, setQuantity] = useState('1')
  const [buyerName, setBuyerName] = useState('')
  const [quantityError, setQuantityError] = useState(null)

  function handleSubmit(event) {
    event.preventDefault()
    const value = Number(quantity)
    if (!Number.isInteger(value) || value < 1) {
      setQuantityError('La cantidad tiene que ser al menos 1')
      return
    }
    if (value > maxQuantity) {
      setQuantityError(`Solo hay ${maxQuantity} en stock`)
      return
    }
    setQuantityError(null)
    onSell({ quantity: value, buyerName: buyerName.trim() || null })
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-wrap items-end gap-2">
      <div className="w-24">
        <Input
          id={`quantity-${productId}`}
          label="Cantidad"
          type="number"
          min="1"
          max={maxQuantity}
          value={quantity}
          onChange={(event) => setQuantity(event.target.value)}
          error={quantityError}
        />
      </div>
      <div className="w-48">
        <Input
          id={`buyer-${productId}`}
          label="Comprador (opcional)"
          value={buyerName}
          onChange={(event) => setBuyerName(event.target.value)}
        />
      </div>
      <Button type="submit" disabled={isPending} className="px-3 py-2 text-sm">
        {isPending ? 'Vendiendo…' : 'Confirmar'}
      </Button>
      <button
        type="button"
        onClick={onCancel}
        className="px-2 py-2 text-sm font-medium text-slate-500 hover:underline"
      >
        Volver
      </button>
      {submitError && <p className="w-full text-sm text-red-600">{submitError}</p>}
    </form>
  )
}
