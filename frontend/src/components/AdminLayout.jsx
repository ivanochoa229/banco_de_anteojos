import { useState } from 'react'
import { Link, NavLink } from 'react-router-dom'
import { useAuth } from '../context/useAuth'
import { BancoAnteojosLogo, HacerFuturoLogo } from './BrandLogo'
import { Button } from './Button'

const ADMIN_NAV_ROW_1 = [
  { to: '/admin', label: 'Inicio', end: true },
  { to: '/admin/solicitantes', label: 'Solicitantes' },
  { to: '/admin/donantes', label: 'Donantes' },
  { to: '/admin/marcos', label: 'Inventario de Marcos' },
  { to: '/admin/asignaciones', label: 'Asignaciones' },
]

const ADMIN_NAV_ROW_2 = [
  { to: '/admin/turnos', label: 'Agenda de Turnos' },
  { to: '/admin/envios', label: 'Envíos (Vía Cargo)' },
  { to: '/admin/catalogo', label: 'Catálogo de Sol' },
  { to: '/admin/probador', label: 'Probador Virtual' },
  { to: '/admin/indicadores', label: 'Indicadores' },
]

function navLinkClass({ isActive }) {
  return `flex items-center justify-center rounded-xl px-3 py-2.5 text-sm font-bold transition-all text-center ${
    isActive
      ? 'bg-orange-600 text-white shadow-sm'
      : 'bg-white border border-slate-200/90 text-slate-700 hover:border-orange-300 hover:bg-orange-50/60 hover:text-orange-700 shadow-2xs'
  }`
}

export function AdminLayout({ children }) {
  const { logout } = useAuth()
  const [isNavOpen, setIsNavOpen] = useState(false)

  return (
    <div className="flex min-h-screen flex-col bg-slate-50/80 text-slate-800">
      {/* Cabecera Principal */}
      <header className="sticky top-0 z-40 border-b border-slate-200 bg-white/95 backdrop-blur-md">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-3 sm:px-6">
            <Link to="/admin" className="flex items-center gap-3 transition hover:opacity-95" title="Panel Administrador">
              <HacerFuturoLogo className="h-10 sm:h-11 w-auto" />
              <div className="h-7 w-[1.5px] bg-slate-200" />
              <BancoAnteojosLogo className="h-12 sm:h-13 w-auto" />
            </Link>

          <div className="flex items-center gap-3">
            <Button
              onClick={logout}
              className="rounded-xl border border-slate-200 bg-white px-3.5 py-1.5 text-xs font-bold text-slate-700 shadow-xs hover:bg-slate-50"
            >
              Cerrar sesión
            </Button>

            {/* Menú móvil */}
            <button
              type="button"
              onClick={() => setIsNavOpen((open) => !open)}
              aria-label="Abrir menú"
              className="rounded-xl border border-slate-200 p-2 text-slate-600 hover:bg-slate-100 sm:hidden"
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

        {/* Barra de navegación en dos filas alineadas (5 columnas cada una) */}
        <div className="hidden border-t border-slate-200/70 bg-slate-50/70 px-4 py-3 sm:block sm:px-6">
          <div className="mx-auto max-w-7xl space-y-2.5">
            <nav className="grid grid-cols-5 gap-3">
              {ADMIN_NAV_ROW_1.map(({ to, label, end }) => (
                <NavLink key={to} to={to} end={end} className={navLinkClass}>
                  {label}
                </NavLink>
              ))}
            </nav>
            <nav className="grid grid-cols-5 gap-3">
              {ADMIN_NAV_ROW_2.map(({ to, label, end }) => (
                <NavLink key={to} to={to} end={end} className={navLinkClass}>
                  {label}
                </NavLink>
              ))}
            </nav>
          </div>
        </div>

        {/* Menú desplegable Móvil */}
        {isNavOpen && (
          <nav className="border-t border-slate-200 bg-white px-4 py-4 sm:hidden">
            <div className="flex flex-col gap-2">
              {[...ADMIN_NAV_ROW_1, ...ADMIN_NAV_ROW_2].map(({ to, label, end }) => (
                <NavLink
                  key={to}
                  to={to}
                  end={end}
                  className={navLinkClass}
                  onClick={() => setIsNavOpen(false)}
                >
                  {label}
                </NavLink>
              ))}
            </div>
          </nav>
        )}
      </header>

      {/* Contenido principal */}
      <main className="mx-auto w-full max-w-7xl flex-1 px-4 py-8 sm:px-6">{children}</main>

      {/* Footer del portal */}
      <footer className="mt-auto border-t border-slate-200 bg-white py-6 text-xs text-slate-500">
        <div className="mx-auto flex max-w-7xl flex-col items-center justify-between gap-4 px-4 sm:flex-row sm:px-6">
          <span>Banco de Anteojos · Portal de Administración · Fundación Hacer Futuro</span>
          <span>Panel de Gestión Integral & Trazabilidad</span>
        </div>
      </footer>
    </div>
  )
}
