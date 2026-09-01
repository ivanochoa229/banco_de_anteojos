import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { useAuth } from '../../../context/useAuth'
import { Alert } from '../../../components/Alert'
import { Button } from '../../../components/Button'
import { Input } from '../../../components/Input'

const DNI_REGEX = /^\d{7,8}$/
const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const TODAY = new Date().toISOString().split('T')[0]

// El alta de solicitante crea un Applicant nuevo (queda pendiente de validación de identidad,
// igual que cuando lo carga un operador) y devuelve sesión iniciada.
export function RegisterPage() {
  const { registerApplicant } = useAuth()
  const navigate = useNavigate()
  const [values, setValues] = useState({
    firstName: '',
    lastName: '',
    dni: '',
    birthDate: '',
    phone: '',
    email: '',
    password: '',
  })
  const [fieldErrors, setFieldErrors] = useState({})

  function setField(name) {
    return (event) => setValues((current) => ({ ...current, [name]: event.target.value }))
  }

  const registerMutation = useMutation({
    mutationFn: () =>
      registerApplicant({
        firstName: values.firstName.trim(),
        lastName: values.lastName.trim(),
        dni: values.dni.trim(),
        birthDate: values.birthDate || null,
        phone: values.phone.trim() || null,
        email: values.email.trim(),
        password: values.password,
      }),
    onSuccess: () => navigate('/', { replace: true }),
  })

  function handleSubmit(event) {
    event.preventDefault()
    const errors = {}
    if (!values.firstName.trim()) errors.firstName = 'Ingresá el nombre'
    if (!values.lastName.trim()) errors.lastName = 'Ingresá el apellido'
    if (!DNI_REGEX.test(values.dni.trim())) errors.dni = 'El DNI debe tener 7 u 8 dígitos'
    if (!values.email.trim()) errors.email = 'Ingresá tu email'
    else if (!EMAIL_REGEX.test(values.email.trim())) errors.email = 'El email no es válido'
    if (values.password.length < 8) errors.password = 'La contraseña debe tener al menos 8 caracteres'
    setFieldErrors(errors)
    if (Object.keys(errors).length === 0) registerMutation.mutate()
  }

  return (
    <main className="flex min-h-screen items-center justify-center bg-slate-100 px-4 py-8">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 shadow-sm">
        <h1 className="text-2xl font-bold text-slate-900">Banco de Anteojos</h1>
        <p className="mt-1 text-sm text-slate-500">Registrate como solicitante</p>

        <form onSubmit={handleSubmit} noValidate className="mt-8 space-y-4">
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
          />
          <Input
            id="birthDate"
            label="Fecha de nacimiento (opcional)"
            type="date"
            max={TODAY}
            value={values.birthDate}
            onChange={setField('birthDate')}
          />
          <Input
            id="phone"
            label="Teléfono (opcional)"
            value={values.phone}
            onChange={setField('phone')}
          />
          <Input
            id="email"
            label="Email"
            type="email"
            value={values.email}
            onChange={setField('email')}
            error={fieldErrors.email}
            autoComplete="email"
          />
          <Input
            id="password"
            label="Contraseña"
            type="password"
            value={values.password}
            onChange={setField('password')}
            error={fieldErrors.password}
            autoComplete="new-password"
          />

          {registerMutation.isError && <Alert>{registerMutation.error.message}</Alert>}

          <Button type="submit" className="w-full" disabled={registerMutation.isPending}>
            {registerMutation.isPending ? 'Creando la cuenta…' : 'Registrarme'}
          </Button>
        </form>

        <p className="mt-6 text-center text-sm text-slate-500">
          ¿Ya tenés cuenta?{' '}
          <Link to="/login" className="font-medium text-sky-700 hover:underline">
            Iniciá sesión
          </Link>
        </p>
      </div>
    </main>
  )
}
