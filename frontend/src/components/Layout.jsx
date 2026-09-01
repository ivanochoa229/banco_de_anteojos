import { useState } from 'react'
import { NavLink } from 'react-router-dom'
import { useAuth } from '../context/useAuth'
import { Button } from './Button'

const ROLE_LABELS = { ADMIN: 'Administrador', OPERATOR: 'Operador', APPLICANT: 'Solicitante' }

const STAFF_NAV_LINKS = [
  { to: '/', label: 'Inicio', end: true },
  { to: '/applicants', label: 'Solicitantes' },
  { to: '/donors', label: 'Donantes' },
  { to: '/frames', label: 'Inventario' },
  { to: '/assignments', label: 'Asignaciones' },
  { to: '/appointments', label: 'Turnos' },
  { to: '/shipments', label: 'Envíos' },
  { to: '/catalog', label: 'Catálogo' },
  { to: '/try-on', label: 'Probador' },
  { to: '/indicators', label: 'Indicadores' },
]

const APPLICANT_NAV_LINKS = [
  { to: '/', label: 'Inicio', end: true },
  { to: '/my-appointments', label: 'Mis turnos' },
  { to: '/my-status', label: 'Estado de mi marco' },
  { to: '/try-on', label: 'Probador' },
]

function navLinkClass({ isActive }) {
  return `block rounded-md px-3 py-1.5 text-sm font-medium transition ${
    isActive ? 'bg-sky-50 text-sky-700' : 'text-slate-600 hover:bg-slate-100'
  }`
}

export function Layout({ children }) {
  const { role, logout } = useAuth()
  const [isNavOpen, setIsNavOpen] = useState(false)
  const navLinks = role === 'APPLICANT' ? APPLICANT_NAV_LINKS : STAFF_NAV_LINKS

  return (
    <div className="min-h-screen bg-slate-100">
      <header className="border-b border-slate-200 bg-white px-4 py-3 sm:px-6">
        <div className="mx-auto flex max-w-5xl items-center justify-between">
          <div className="flex items-center gap-8">
            <div>
              <h1 className="text-lg font-bold text-slate-900">Banco de Anteojos</h1>
              <p className="text-xs text-slate-500">Fundación Hacer Futuro</p>
            </div>
            <nav className="hidden gap-1 sm:flex sm:flex-wrap">
              {navLinks.map(({ to, label, end }) => (
                <NavLink key={to} to={to} end={end} className={navLinkClass}>
                  {label}
                </NavLink>
              ))}
            </nav>
          </div>
          <div className="flex items-center gap-2 sm:gap-4">
            {role && (
              <span className="hidden rounded-full bg-sky-50 px-3 py-1 text-xs font-medium text-sky-700 sm:inline">
                {ROLE_LABELS[role] ?? role}
              </span>
            )}
            <Button
              onClick={logout}
              className="hidden bg-slate-600 px-3 py-1.5 text-sm hover:bg-slate-700 sm:inline-flex"
            >
              Cerrar sesión
            </Button>
            <button
              type="button"
              onClick={() => setIsNavOpen((open) => !open)}
              aria-label="Abrir el menú"
              aria-expanded={isNavOpen}
              className="rounded-md border border-slate-300 p-2 text-slate-600 sm:hidden"
            >
              <svg viewBox="0 0 24 24" className="h-5 w-5" fill="none" stroke="currentColor" strokeWidth="2">
                {isNavOpen ? (
                  <path strokeLinecap="round" d="M6 6l12 12M18 6L6 18" />
                ) : (
                  <path strokeLinecap="round" d="M4 7h16M4 12h16M4 17h16" />
                )}
              </svg>
            </button>
          </div>
        </div>

        {isNavOpen && (
          <nav className="mx-auto mt-3 flex max-w-5xl flex-col gap-1 border-t border-slate-100 pt-3 sm:hidden">
            {navLinks.map(({ to, label, end }) => (
              <NavLink key={to} to={to} end={end} className={navLinkClass} onClick={() => setIsNavOpen(false)}>
                {label}
              </NavLink>
            ))}
            {role && (
              <span className="mt-1 w-fit rounded-full bg-sky-50 px-3 py-1 text-xs font-medium text-sky-700">
                {ROLE_LABELS[role] ?? role}
              </span>
            )}
            <Button onClick={logout} className="mt-2 bg-slate-600 px-3 py-1.5 text-sm hover:bg-slate-700">
              Cerrar sesión
            </Button>
          </nav>
        )}
      </header>

      <main className="mx-auto max-w-5xl px-4 py-8 sm:px-6">{children}</main>
    </div>
  )
}
