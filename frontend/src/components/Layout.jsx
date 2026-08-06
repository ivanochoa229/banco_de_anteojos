import { NavLink } from 'react-router-dom'
import { useAuth } from '../context/useAuth'
import { Button } from './Button'

const ROLE_LABELS = { ADMIN: 'Administrador', OPERATOR: 'Operador' }

function navLinkClass({ isActive }) {
  return `rounded-md px-3 py-1.5 text-sm font-medium transition ${
    isActive ? 'bg-sky-50 text-sky-700' : 'text-slate-600 hover:bg-slate-100'
  }`
}

export function Layout({ children }) {
  const { role, logout } = useAuth()

  return (
    <div className="min-h-screen bg-slate-100">
      <header className="border-b border-slate-200 bg-white px-6 py-3">
        <div className="mx-auto flex max-w-5xl items-center justify-between">
          <div className="flex items-center gap-8">
            <div>
              <h1 className="text-lg font-bold text-slate-900">Banco de Anteojos</h1>
              <p className="text-xs text-slate-500">Fundación Hacer Futuro</p>
            </div>
            <nav className="flex gap-1">
              <NavLink to="/" end className={navLinkClass}>
                Inicio
              </NavLink>
              <NavLink to="/applicants" className={navLinkClass}>
                Solicitantes
              </NavLink>
              <NavLink to="/donors" className={navLinkClass}>
                Donantes
              </NavLink>
              <NavLink to="/frames" className={navLinkClass}>
                Inventario
              </NavLink>
              <NavLink to="/assignments" className={navLinkClass}>
                Asignaciones
              </NavLink>
            </nav>
          </div>
          <div className="flex items-center gap-4">
            {role && (
              <span className="rounded-full bg-sky-50 px-3 py-1 text-xs font-medium text-sky-700">
                {ROLE_LABELS[role] ?? role}
              </span>
            )}
            <Button onClick={logout} className="bg-slate-600 px-3 py-1.5 text-sm hover:bg-slate-700">
              Cerrar sesión
            </Button>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-5xl px-6 py-8">{children}</main>
    </div>
  )
}
