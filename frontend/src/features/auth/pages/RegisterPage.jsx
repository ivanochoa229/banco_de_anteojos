import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { useAuth } from '../../../context/useAuth'
import { Alert } from '../../../components/Alert'
import { BancoAnteojosLogo, HacerFuturoLogo } from '../../../components/BrandLogo'
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
    onSuccess: () => navigate('/solicitante', { replace: true }),
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
    <main className="relative flex min-h-screen items-center justify-center overflow-hidden bg-slate-100/90 px-4 py-12">
      {/* Círculos decorativos difuminados */}
      <div className="pointer-events-none absolute -top-40 -left-40 h-96 w-96 rounded-full bg-orange-200/25 blur-3xl" />
      <div className="pointer-events-none absolute -bottom-40 -right-40 h-96 w-96 rounded-full bg-slate-300/30 blur-3xl" />

      <div className="relative w-full max-w-lg rounded-3xl border border-slate-200 bg-white p-8 shadow-xl shadow-slate-900/5 sm:p-10">
        {/* Link para volver a la home pública */}
        <div className="mb-6">
          <Link
            to="/"
            className="inline-flex items-center gap-1.5 text-xs font-bold text-slate-500 hover:text-orange-600 transition"
          >
            <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
              <path strokeLinecap="round" strokeLinejoin="round" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
            <span>Volver a la página principal</span>
          </Link>
        </div>
        <div className="text-center">
          <div className="mb-6 flex items-center justify-center gap-4">
            <HacerFuturoLogo className="h-11 w-auto" />
            <div className="h-8 w-[1.5px] bg-slate-200" />
            <BancoAnteojosLogo className="h-14 w-auto" />
          </div>

          <span className="block text-[11px] font-black uppercase tracking-widest text-slate-500">
            Fundación Hacer Futuro
          </span>
          <h1 className="mt-1 text-2xl font-black tracking-tight text-orange-600 sm:text-3xl">
            REGISTRO DE SOLICITANTE
          </h1>

          <p className="mt-2 text-xs font-medium text-slate-500">
            Completá tus datos para solicitar tus anteojos y agendar tu turno
          </p>
        </div>


        <form onSubmit={handleSubmit} noValidate className="mt-8 space-y-4">
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Input
              id="firstName"
              label="Nombre"
              placeholder="Juan"
              value={values.firstName}
              onChange={setField('firstName')}
              error={fieldErrors.firstName}
            />
            <Input
              id="lastName"
              label="Apellido"
              placeholder="Pérez"
              value={values.lastName}
              onChange={setField('lastName')}
              error={fieldErrors.lastName}
            />
          </div>
          <Input
            id="dni"
            label="DNI (sin puntos)"
            placeholder="12345678"
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
            label="Teléfono / WhatsApp (opcional)"
            placeholder="381 123 4567"
            value={values.phone}
            onChange={setField('phone')}
          />
          <Input
            id="email"
            label="Correo electrónico"
            placeholder="juanperez@gmail.com"
            type="email"
            value={values.email}
            onChange={setField('email')}
            error={fieldErrors.email}
            autoComplete="email"
          />
          <Input
            id="password"
            label="Contraseña"
            placeholder="Mínimo 8 caracteres"
            type="password"
            value={values.password}
            onChange={setField('password')}
            error={fieldErrors.password}
            autoComplete="new-password"
          />

          {registerMutation.isError && <Alert>{registerMutation.error.message}</Alert>}

          <Button
            type="submit"
            className="w-full py-3 text-sm font-bold shadow-md shadow-orange-600/20"
            disabled={registerMutation.isPending}
          >
            {registerMutation.isPending ? 'Creando la cuenta…' : 'Registrarme como beneficiario'}
          </Button>
        </form>

        <div className="mt-8 border-t border-slate-100 pt-5 text-center">
          <p className="text-sm text-slate-600">
            ¿Ya tenés cuenta?{' '}
            <Link
              to="/login"
              className="font-bold text-orange-600 hover:text-orange-700 hover:underline"
            >
              Iniciá sesión acá
            </Link>
          </p>
        </div>
      </div>
    </main>
  )
}

