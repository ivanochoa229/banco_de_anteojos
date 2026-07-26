import { useState } from 'react'
import { Alert } from '../../../components/Alert'
import { Button } from '../../../components/Button'
import { Input } from '../../../components/Input'

const DNI_REGEX = /^\d{7,8}$/
const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

const TODAY = new Date().toISOString().split('T')[0]

// dniEditable: el DNI solo se carga en el alta; en la edición el backend no lo acepta.
export function ApplicantForm({ initialValues, dniEditable, submitLabel, onSubmit, isPending, submitError }) {
  const [values, setValues] = useState({
    firstName: initialValues?.firstName ?? '',
    lastName: initialValues?.lastName ?? '',
    dni: initialValues?.dni ?? '',
    birthDate: initialValues?.birthDate ?? '',
    phone: initialValues?.phone ?? '',
    email: initialValues?.email ?? '',
  })
  const [fieldErrors, setFieldErrors] = useState({})

  function setField(name) {
    return (event) => setValues((current) => ({ ...current, [name]: event.target.value }))
  }

  function handleSubmit(event) {
    event.preventDefault()
    const errors = {}
    if (!values.firstName.trim()) errors.firstName = 'Ingresá el nombre'
    if (!values.lastName.trim()) errors.lastName = 'Ingresá el apellido'
    if (dniEditable && !DNI_REGEX.test(values.dni.trim())) errors.dni = 'El DNI debe tener 7 u 8 dígitos'
    if (values.email.trim() && !EMAIL_REGEX.test(values.email.trim())) errors.email = 'El email no es válido'
    setFieldErrors(errors)
    if (Object.keys(errors).length > 0) return

    onSubmit({
      firstName: values.firstName.trim(),
      lastName: values.lastName.trim(),
      ...(dniEditable ? { dni: values.dni.trim() } : {}),
      birthDate: values.birthDate || null,
      phone: values.phone.trim() || null,
      email: values.email.trim() || null,
    })
  }

  return (
    <form onSubmit={handleSubmit} noValidate className="max-w-xl space-y-4">
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <Input
          id="firstName"
          label="Nombre"
          value={values.firstName}
          onChange={setField('firstName')}
          error={fieldErrors.firstName}
        />
        <Input
          id="lastName"
          label="Apellido"
          value={values.lastName}
          onChange={setField('lastName')}
          error={fieldErrors.lastName}
        />
      </div>
      <Input
        id="dni"
        label="DNI"
        inputMode="numeric"
        value={values.dni}
        onChange={setField('dni')}
        error={fieldErrors.dni}
        disabled={!dniEditable}
      />
      <Input
        id="birthDate"
        label="Fecha de nacimiento (opcional)"
        type="date"
        max={TODAY}
        value={values.birthDate}
        onChange={setField('birthDate')}
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

      {submitError && <Alert>{submitError}</Alert>}

      <Button type="submit" disabled={isPending}>
        {isPending ? 'Guardando…' : submitLabel}
      </Button>
    </form>
  )
}
