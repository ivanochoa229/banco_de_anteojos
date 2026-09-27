import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { useAuth } from '../../../context/useAuth'
import { Alert } from '../../../components/Alert'
import { BancoAnteojosLogo, HacerFuturoLogo } from '../../../components/BrandLogo'
import { Button } from '../../../components/Button'
import { Input } from '../../../components/Input'

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

export function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [fieldErrors, setFieldErrors] = useState({})

  const loginMutation = useMutation({
    mutationFn: () => login(email.trim(), password),
    onSuccess: (userRole) => {
      // Redirección inteligente al portal correspondiente según el rol del usuario
      if (userRole === 'APPLICANT') {
        navigate('/solicitante', { replace: true })
      } else if (userRole === 'OPERATOR') {
        navigate('/operador', { replace: true })
      } else if (userRole === 'ADMIN') {
        navigate('/admin', { replace: true })
      } else {
        navigate('/', { replace: true })
      }
    },
  })

  function handleSubmit(event) {
    event.preventDefault()
    const errors = {}
    if (!email.trim()) errors.email = 'Ingresá tu correo electrónico'
    else if (!EMAIL_REGEX.test(email.trim())) errors.email = 'El correo electrónico no es válido'
    if (!password) errors.password = 'Ingresá tu contraseña'
    setFieldErrors(errors)
    if (Object.keys(errors).length === 0) loginMutation.mutate()
  }

  return (
    <main className="relative flex min-h-screen items-center justify-center overflow-hidden bg-slate-100/90 px-4 py-12">
      <div className="pointer-events-none absolute -top-40 -left-40 h-96 w-96 rounded-full bg-orange-200/25 blur-3xl" />
      <div className="pointer-events-none absolute -bottom-40 -right-40 h-96 w-96 rounded-full bg-slate-300/30 blur-3xl" />

      <div className="relative w-full max-w-md rounded-3xl border border-slate-200 bg-white p-8 shadow-xl shadow-slate-900/5 sm:p-10">
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
          {/* Logos oficiales combinados */}
          <div className="mb-5 flex items-center justify-center gap-4">
            <HacerFuturoLogo className="h-11 w-auto" />
            <div className="h-8 w-[1.5px] bg-slate-200" />
            <BancoAnteojosLogo className="h-14 w-auto" />
          </div>

          <span className="block text-[11px] font-black uppercase tracking-widest text-slate-500">
            Fundación Hacer Futuro
          </span>
          <h1 className="mt-1 text-2xl font-black tracking-tight text-orange-600 sm:text-3xl">
            BANCO DE ANTEOJOS
          </h1>
          <p className="mt-2 text-xs font-medium text-slate-500">
            Iniciá sesión para acceder a tu portal correspondiente
          </p>
        </div>

        <form onSubmit={handleSubmit} noValidate className="mt-8 space-y-4">
          <Input
            id="email"
            label="Correo electrónico"
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            error={fieldErrors.email}
            autoComplete="email"
            placeholder="ejemplo@bancodeanteojos.org"
          />
          <Input
            id="password"
            label="Contraseña"
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            error={fieldErrors.password}
            autoComplete="current-password"
            placeholder="••••••••••••"
          />

          {loginMutation.isError && <Alert>{loginMutation.error.message}</Alert>}

          <Button
            type="submit"
            className="w-full py-3 text-sm font-bold shadow-md shadow-orange-600/20"
            disabled={loginMutation.isPending}
          >
            {loginMutation.isPending ? 'Iniciando sesión…' : 'Iniciar sesión'}
          </Button>
        </form>

        <div className="mt-8 border-t border-slate-100 pt-5 text-center">
          <p className="text-sm text-slate-600">
            ¿Sos solicitante y no tenés cuenta?{' '}
            <Link
              to="/register"
              className="font-bold text-orange-600 hover:text-orange-700 hover:underline"
            >
              Registrate acá
            </Link>
          </p>
          <p className="mt-4 text-xs text-slate-400">
            San Miguel de Tucumán · Salud Visual Ecosocial
          </p>
        </div>
      </div>
    </main>
  )
}
