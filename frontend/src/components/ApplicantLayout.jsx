import { useState } from 'react'
import { Link, NavLink } from 'react-router-dom'
import { useAuth } from '../context/useAuth'
import { BancoAnteojosLogo, HacerFuturoLogo } from './BrandLogo'
import { Button } from './Button'

const APPLICANT_NAV_LINKS = [
  { to: '/solicitante', label: 'Inicio', end: true },
  { to: '/solicitante/mi-solicitud', label: 'Mi Solicitud' },
  { to: '/solicitante/mi-receta', label: 'Mi Receta' },
  { to: '/solicitante/mi-marco', label: 'Estado de mi Marco' },
  { to: '/solicitante/mis-turnos', label: 'Mis Turnos' },
  { to: '/solicitante/probador', label: 'Probador Virtual' },
  { to: '/solicitante/catalogo', label: 'Catálogo de Sol' },
]

function navLinkClass({ isActive }) {
  return `flex items-center justify-center rounded-xl px-3 py-2 text-xs sm:text-sm font-bold transition-all text-center ${
    isActive
      ? 'bg-orange-600 text-white shadow-sm'
      : 'bg-white border border-slate-200/90 text-slate-700 hover:border-orange-300 hover:bg-orange-50/60 hover:text-orange-700'
  }`
}

export function ApplicantLayout({ children }) {
  const { logout } = useAuth()
  const [isNavOpen, setIsNavOpen] = useState(false)

  return (
    <div className="flex min-h-screen flex-col bg-slate-50/80 text-slate-800">
      {/* Cabecera Principal del Solicitante */}
      <header className="sticky top-0 z-40 border-b border-slate-200 bg-white/95 backdrop-blur-md">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-3 sm:px-6">
            <Link to="/solicitante" className="flex items-center gap-3 transition hover:opacity-95" title="Portal del Solicitante">
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

        {/* Barra de navegación del solicitante */}
        <div className="hidden border-t border-slate-200/70 bg-slate-50/70 px-4 py-3 sm:block sm:px-6">
          <nav className="mx-auto grid max-w-7xl grid-cols-7 gap-2.5">
            {APPLICANT_NAV_LINKS.map(({ to, label, end }) => (
              <NavLink key={to} to={to} end={end} className={navLinkClass}>
                {label}
              </NavLink>
            ))}
          </nav>
        </div>

        {/* Menú móvil */}
        {isNavOpen && (
          <nav className="border-t border-slate-200 bg-white px-4 py-4 sm:hidden">
            <div className="flex flex-col gap-2">
              {APPLICANT_NAV_LINKS.map(({ to, label, end }) => (
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

      {/* Contenido */}
      <main className="mx-auto w-full max-w-7xl flex-1 px-4 py-8 sm:px-6">{children}</main>

      {/* Footer personal */}
      <footer className="mt-auto border-t border-slate-200 bg-white py-6 text-xs text-slate-500">
        <div className="mx-auto flex max-w-7xl flex-col items-center justify-between gap-4 px-4 sm:flex-row sm:px-6">
          <span>Banco de Anteojos · Fundación Hacer Futuro · Atención con turno previo</span>
          <div className="flex items-center gap-4">
            <a href="https://wa.me/5493813982020" target="_blank" rel="noopener noreferrer" className="font-bold text-orange-600 hover:underline">
              Consultas por WhatsApp: 381-398-2020
            </a>
          </div>
        </div>
      </footer>
    </div>
  )
}
