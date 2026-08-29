import { useState } from 'react'
import { Alert } from '../../../components/Alert'
import { Button } from '../../../components/Button'
import { Input } from '../../../components/Input'

export function ProductForm({ initialValues, onSubmit, isPending, submitError, submitLabel }) {
  const [values, setValues] = useState({
    name: initialValues?.name ?? '',
    description: initialValues?.description ?? '',
    price: initialValues?.price ?? '',
    stockQuantity: initialValues?.stockQuantity ?? '',
  })
  const [fieldErrors, setFieldErrors] = useState({})

  function setField(name) {
    return (event) => setValues((current) => ({ ...current, [name]: event.target.value }))
  }

  function handleSubmit(event) {
    event.preventDefault()
    const errors = {}
    if (!values.name.trim()) errors.name = 'Ingresá el nombre del producto'
    const price = Number(values.price)
    if (values.price === '' || Number.isNaN(price) || price < 0) {
      errors.price = 'Ingresá un precio válido'
    }
    const stockQuantity = Number(values.stockQuantity)
    if (values.stockQuantity === '' || !Number.isInteger(stockQuantity) || stockQuantity < 0) {
      errors.stockQuantity = 'Ingresá un stock válido'
    }
    setFieldErrors(errors)
    if (Object.keys(errors).length > 0) return

    onSubmit({
      name: values.name.trim(),
      description: values.description.trim() || null,
      price,
      stockQuantity,
    })
  }

  return (
    <form onSubmit={handleSubmit} noValidate className="max-w-xl space-y-4">
      <Input id="name" label="Nombre" value={values.name} onChange={setField('name')} error={fieldErrors.name} />
      <Input
        id="description"
        label="Descripción (opcional)"
        value={values.description}
        onChange={setField('description')}
      />
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <Input
          id="price"
          label="Precio"
          type="number"
          min="0"
          step="0.01"
          value={values.price}
          onChange={setField('price')}
          error={fieldErrors.price}
        />
        <Input
          id="stockQuantity"
          label="Stock"
          type="number"
          min="0"
          step="1"
          value={values.stockQuantity}
          onChange={setField('stockQuantity')}
          error={fieldErrors.stockQuantity}
        />
      </div>

      {submitError && <Alert>{submitError}</Alert>}

      <Button type="submit" disabled={isPending}>
        {isPending ? 'Guardando…' : submitLabel}
      </Button>
    </form>
  )
}
