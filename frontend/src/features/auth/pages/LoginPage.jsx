import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { useAuth } from '../../../context/useAuth'
import { Alert } from '../../../components/Alert'
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
    onSuccess: () => navigate('/', { replace: true }),
  })

  function handleSubmit(event) {
    event.preventDefault()
    const errors = {}
    if (!email.trim()) errors.email = 'Ingresá tu email'
    else if (!EMAIL_REGEX.test(email.trim())) errors.email = 'El email no es válido'
    if (!password) errors.password = 'Ingresá tu contraseña'
    setFieldErrors(errors)
    if (Object.keys(errors).length === 0) loginMutation.mutate()
  }

  return (
    <main className="flex min-h-screen items-center justify-center bg-slate-100 px-4">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 shadow-sm">
        <h1 className="text-2xl font-bold text-slate-900">Banco de Anteojos</h1>
        <p className="mt-1 text-sm text-slate-500">Fundación Hacer Futuro</p>

        <form onSubmit={handleSubmit} noValidate className="mt-8 space-y-4">
          <Input
            id="email"
            label="Email"
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            error={fieldErrors.email}
            autoComplete="email"
          />
          <Input
            id="password"
            label="Contraseña"
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            error={fieldErrors.password}
            autoComplete="current-password"
          />

          {loginMutation.isError && <Alert>{loginMutation.error.message}</Alert>}

          <Button type="submit" className="w-full" disabled={loginMutation.isPending}>
            {loginMutation.isPending ? 'Ingresando…' : 'Ingresar'}
          </Button>
        </form>
      </div>
    </main>
  )
}
