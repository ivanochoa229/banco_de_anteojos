import { useState } from 'react'
import { Alert } from '../../../components/Alert'
import { Button } from '../../../components/Button'
import { Input } from '../../../components/Input'
import { Select } from '../../../components/Select'
import { DONOR_TYPE_OPTIONS } from '../labels'

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

export function DonorForm({ onSubmit, isPending, submitError }) {
  const [values, setValues] = useState({
    donorType: 'INDIVIDUAL',
    name: '',
    documentNumber: '',
    phone: '',
    email: '',
    address: '',
    postalCode: '',
    city: '',
    province: '',
  })
  const [fieldErrors, setFieldErrors] = useState({})

  const isOrganization = values.donorType === 'ORGANIZATION'

  function setField(name) {
    return (event) => setValues((current) => ({ ...current, [name]: event.target.value }))
  }

  function handleSubmit(event) {
    event.preventDefault()
    const errors = {}
    if (!values.name.trim()) {
      errors.name = isOrganization ? 'Ingresá la razón social' : 'Ingresá el nombre y apellido'
    }
    if (values.email.trim() && !EMAIL_REGEX.test(values.email.trim())) {
      errors.email = 'El email no es válido'
    }
    setFieldErrors(errors)
    if (Object.keys(errors).length > 0) return

    onSubmit({
      donorType: values.donorType,
      name: values.name.trim(),
      documentNumber: values.documentNumber.trim() || null,
      phone: values.phone.trim() || null,
      email: values.email.trim() || null,
      address: values.address.trim() || null,
      postalCode: values.postalCode.trim() || null,
      city: values.city.trim() || null,
      province: values.province.trim() || null,
    })
  }

  return (
    <form onSubmit={handleSubmit} noValidate className="max-w-xl space-y-4">
      <Select
        id="donorType"
        label="Tipo de donante"
        options={DONOR_TYPE_OPTIONS}
        value={values.donorType}
        onChange={setField('donorType')}
      />
      <Input
        id="name"
        label={isOrganization ? 'Razón social' : 'Nombre y apellido'}
        value={values.name}
        onChange={setField('name')}
        error={fieldErrors.name}
      />
      <Input
        id="documentNumber"
        label={isOrganization ? 'CUIT (opcional)' : 'DNI (opcional)'}
        inputMode="numeric"
        value={values.documentNumber}
        onChange={setField('documentNumber')}
      />
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <Input
          id="phone"
          label="Teléfono (opcional)"
          value={values.phone}
          onChange={setField('phone')}
        />
        <Input
          id="email"
          label="Email (opcional)"
          type="email"
          value={values.email}
          onChange={setField('email')}
          error={fieldErrors.email}
        />
      </div>

      <fieldset className="space-y-4 rounded-lg border border-slate-200 p-4">
        <legend className="px-1 text-sm font-semibold text-slate-700">Domicilio (opcional)</legend>
        <p className="text-sm text-slate-500">
          Lo pide la etiqueta de envío de donaciones por Vía Cargo.
        </p>
        <Input
          id="address"
          label="Domicilio"
          maxLength={255}
          value={values.address}
          onChange={setField('address')}
        />
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
          <Input
            id="city"
            label="Localidad"
            maxLength={120}
            value={values.city}
            onChange={setField('city')}
          />
          <Input
            id="province"
            label="Provincia"
            maxLength={60}
            value={values.province}
            onChange={setField('province')}
          />
          <Input
            id="postalCode"
            label="CP"
            maxLength={10}
            value={values.postalCode}
            onChange={setField('postalCode')}
          />
        </div>
      </fieldset>

      {submitError && <Alert>{submitError}</Alert>}

      <Button type="submit" disabled={isPending}>
        {isPending ? 'Guardando…' : 'Registrar donante'}
      </Button>
    </form>
  )
}
