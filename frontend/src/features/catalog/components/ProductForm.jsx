import { useEffect, useRef, useState } from 'react'
import { Alert } from '../../../components/Alert'
import { Button } from '../../../components/Button'
import { Input } from '../../../components/Input'
import { ACCEPTED_IMAGE_TYPES, validateImageFile } from '../imageFile'

// Con withImage (alta) el formulario también pide la foto, opcional: onSubmit recibe (data, file).
// En la edición la foto se cambia aparte, con ProductImageUpload.
export function ProductForm({ initialValues, onSubmit, isPending, submitError, submitLabel, withImage = false }) {
  const [values, setValues] = useState({
    name: initialValues?.name ?? '',
    description: initialValues?.description ?? '',
    price: initialValues?.price ?? '',
  })
  const [image, setImage] = useState(null)
  const [fieldErrors, setFieldErrors] = useState({})
  // La URL local de la vista previa retiene el archivo en memoria hasta que se libera: se libera
  // al reemplazar la foto y, la última, al desmontar.
  const previewUrlRef = useRef(null)

  useEffect(() => () => previewUrlRef.current && URL.revokeObjectURL(previewUrlRef.current), [])

  function replaceImage(file) {
    if (previewUrlRef.current) URL.revokeObjectURL(previewUrlRef.current)
    previewUrlRef.current = file ? URL.createObjectURL(file) : null
    setImage(file ? { file, previewUrl: previewUrlRef.current } : null)
  }

  function setField(name) {
    return (event) => setValues((current) => ({ ...current, [name]: event.target.value }))
  }

  function handleFileChange(event) {
    const selected = event.target.files[0]
    if (!selected) return
    const error = validateImageFile(selected)
    setFieldErrors((current) => ({ ...current, image: error }))
    if (error) {
      event.target.value = ''
      replaceImage(null)
      return
    }
    replaceImage(selected)
  }

  function handleSubmit(event) {
    event.preventDefault()
    const errors = {}
    if (!values.name.trim()) errors.name = 'Ingresá el nombre del anteojo'
    const price = Number(values.price)
    if (values.price === '' || Number.isNaN(price) || price < 0) {
      errors.price = 'Ingresá un precio válido'
    }
    setFieldErrors(errors)
    if (Object.keys(errors).length > 0) return

    onSubmit(
      {
        name: values.name.trim(),
        description: values.description.trim() || null,
        price,
      },
      image?.file ?? null,
    )
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
      <div className="sm:w-1/2">
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
      </div>

      {withImage && (
        <div>
          <Input
            id="image"
            label="Foto (opcional)"
            type="file"
            accept={ACCEPTED_IMAGE_TYPES.join(',')}
            onChange={handleFileChange}
            error={fieldErrors.image}
          />
          {image && (
            <img
              src={image.previewUrl}
              alt="Vista previa de la foto"
              className="mt-3 h-40 w-40 rounded-lg border border-slate-200 object-cover"
            />
          )}
        </div>
      )}

      {submitError && <Alert>{submitError}</Alert>}

      <Button type="submit" disabled={isPending}>
        {isPending ? 'Guardando…' : submitLabel}
      </Button>
    </form>
  )
}
