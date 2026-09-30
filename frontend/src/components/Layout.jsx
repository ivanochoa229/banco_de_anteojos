import { useState } from 'react'
import { Link, NavLink, useLocation } from 'react-router-dom'
import { useAuth } from '../context/useAuth'
import { BancoAnteojosLogo, HacerFuturoLogo } from './BrandLogo'
import { Button } from './Button'

const APPLICANT_NAV_LINKS = [
  { to: '/solicitante', label: 'Inicio', end: true, matchPrefix: ['/solicitante'] },
  { to: '/solicitante/mis-turnos', label: 'Mis Turnos', matchPrefix: ['/solicitante/mis-turnos', '/my-appointments'] },
  { to: '/solicitante/mi-marco', label: 'Estado de mi Marco', matchPrefix: ['/solicitante/mi-marco', '/my-status'] },
  { to: '/solicitante/probador', label: 'Probador Virtual', matchPrefix: ['/solicitante/probador', '/try-on'] },
  { to: '/solicitante/catalogo', label: 'Catálogo de Sol', matchPrefix: ['/solicitante/catalogo', '/catalog'] },
]

const OPERATOR_NAV_ROW_1 = [
  { to: '/operador', label: 'Inicio', end: true, matchPrefix: ['/operador'] },
  { to: '/operador/solicitantes', label: 'Solicitantes', matchPrefix: ['/operador/solicitantes', '/applicants'] },
  { to: '/operador/donantes', label: 'Donantes', matchPrefix: ['/operador/donantes', '/donors'] },
  { to: '/operador/marcos', label: 'Inventario de Marcos', matchPrefix: ['/operador/marcos', '/frames'] },
  { to: '/operador/asignaciones', label: 'Asignaciones', matchPrefix: ['/operador/asignaciones', '/assignments'] },
]

const OPERATOR_NAV_ROW_2 = [
  { to: '/operador/turnos', label: 'Agenda de Turnos', matchPrefix: ['/operador/turnos', '/operador/dias-de-atencion', '/appointments'] },
  { to: '/operador/envios', label: 'Envíos (Vía Cargo)', matchPrefix: ['/operador/envios', '/shipments'] },
  { to: '/operador/probador', label: 'Probador Virtual', matchPrefix: ['/operador/probador', '/try-on'] },
  { to: '/operador/catalogo', label: 'Catálogo de Sol', matchPrefix: ['/operador/catalogo', '/catalog'] },
  { to: '/operador/indicadores', label: 'Indicadores', matchPrefix: ['/operador/indicadores', '/indicators'] },
]

const ADMIN_NAV_ROW_1 = [
  { to: '/admin', label: 'Inicio', end: true, matchPrefix: ['/admin'] },
  { to: '/admin/solicitantes', label: 'Solicitantes', matchPrefix: ['/admin/solicitantes', '/applicants'] },
  { to: '/admin/donantes', label: 'Donantes', matchPrefix: ['/admin/donantes', '/donors'] },
  { to: '/admin/marcos', label: 'Inventario de Marcos', matchPrefix: ['/admin/marcos', '/frames'] },
  { to: '/admin/asignaciones', label: 'Asignaciones', matchPrefix: ['/admin/asignaciones', '/assignments'] },
]

const ADMIN_NAV_ROW_2 = [
  { to: '/admin/turnos', label: 'Agenda de Turnos', matchPrefix: ['/admin/turnos', '/admin/dias-de-atencion', '/appointments'] },
  { to: '/admin/envios', label: 'Envíos (Vía Cargo)', matchPrefix: ['/admin/envios', '/shipments'] },
  { to: '/admin/catalogo', label: 'Catálogo de Sol', matchPrefix: ['/admin/catalogo', '/catalog'] },
  { to: '/admin/probador', label: 'Probador Virtual', matchPrefix: ['/admin/probador', '/try-on'] },
  { to: '/admin/indicadores', label: 'Indicadores', matchPrefix: ['/admin/indicadores', '/indicators'] },
]

export function Layout({ children }) {
  const { role, logout } = useAuth()
  const location = useLocation()
  const [isNavOpen, setIsNavOpen] = useState(false)

  const isApplicant = role === 'APPLICANT'
  const isOperator = role === 'OPERATOR'
  const isAdmin = role === 'ADMIN'

  // Decide active link based on pathname or exact match
  function getNavLinkClass({ to, end, matchPrefix }) {
    const pathname = location.pathname
    const isExact = pathname === to
    const isPrefixMatch =
      matchPrefix && !end && matchPrefix.some((prefix) => pathname.startsWith(prefix))
    const isActive = end ? isExact : isExact || isPrefixMatch

    return `flex items-center justify-center rounded-xl px-3 py-2.5 text-sm font-bold transition-all text-center ${
      isActive
        ? 'bg-orange-600 text-white shadow-sm'
        : 'bg-white border border-slate-200/90 text-slate-700 hover:border-orange-300 hover:bg-orange-50/60 hover:text-orange-700 shadow-2xs'
    }`
  }

  const row1 = isAdmin ? ADMIN_NAV_ROW_1 : OPERATOR_NAV_ROW_1
  const row2 = isAdmin ? ADMIN_NAV_ROW_2 : OPERATOR_NAV_ROW_2

  return (
    <div className="flex min-h-screen flex-col bg-slate-50/80 text-slate-800">
      {/* Cabecera Principal */}
      <header className="sticky top-0 z-40 border-b border-slate-200 bg-white/95 backdrop-blur-md">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-3 sm:px-6">
          {/* Lado izquierdo: Logos oficiales */}
          <Link
            to={isApplicant ? '/solicitante' : isOperator ? '/operador' : '/admin'}
            className="flex items-center gap-3 sm:gap-4 transition hover:opacity-95"
          >
            <HacerFuturoLogo className="h-10 sm:h-11 w-auto" />
            <div className="h-7 w-[1.5px] bg-slate-200" />
            <BancoAnteojosLogo className="h-12 sm:h-13 w-auto" />
          </Link>

          {/* Lado derecho: logout */}
          <div className="flex items-center gap-3">
            <Button
              onClick={logout}
              className="rounded-xl border border-slate-200 bg-white px-3.5 py-1.5 text-xs font-bold text-slate-700 shadow-xs hover:border-slate-300 hover:bg-slate-50 hover:text-slate-900"
            >
              Cerrar sesión
            </Button>

            {/* Botón hamburguesa móvil */}
            <button
              type="button"
              onClick={() => setIsNavOpen((open) => !open)}
              aria-label="Abrir menú"
              aria-expanded={isNavOpen}
              className="rounded-xl border border-slate-200 p-2 text-slate-600 transition hover:bg-slate-100 sm:hidden"
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

        {/* Barra de Navegación */}
        <div className="hidden border-t border-slate-200/70 bg-slate-50/70 px-4 py-3 sm:block sm:px-6">
          <div className="mx-auto max-w-7xl">
            {isApplicant ? (
              // Barra para Solicitante (5 botones grandes bien alineados)
              <nav className="grid grid-cols-5 gap-3">
                {APPLICANT_NAV_LINKS.map((item) => (
                  <NavLink
                    key={item.to}
                    to={item.to}
                    end={item.end}
                    className={() => getNavLinkClass(item)}
                  >
                    {item.label}
                  </NavLink>
                ))}
              </nav>
            ) : (
              // Barra para Staff en 2 filas alineadas en un grid perfecto de 5 columnas
              <div className="space-y-2.5">
                <nav className="grid grid-cols-5 gap-3">
                  {row1.map((item) => (
                    <NavLink
                      key={item.to}
                      to={item.to}
                      end={item.end}
                      className={() => getNavLinkClass(item)}
                    >
                      {item.label}
                    </NavLink>
                  ))}
                </nav>

                <nav className="grid grid-cols-5 gap-3">
                  {row2.map((item) => (
                    <NavLink
                      key={item.to}
                      to={item.to}
                      end={item.end}
                      className={() => getNavLinkClass(item)}
                    >
                      {item.label}
                    </NavLink>
                  ))}
                </nav>
              </div>
            )}
          </div>
        </div>

        {/* Menú desplegable Móvil */}
        {isNavOpen && (
          <nav className="border-t border-slate-200 bg-white px-4 py-4 sm:hidden">
            <div className="flex flex-col gap-1.5">
              {(isApplicant ? APPLICANT_NAV_LINKS : [...row1, ...row2]).map((item) => (
                <NavLink
                  key={item.to}
                  to={item.to}
                  end={item.end}
                  className={() => getNavLinkClass(item)}
                  onClick={() => setIsNavOpen(false)}
                >
                  {item.label}
                </NavLink>
              ))}
            </div>
          </nav>
        )}
      </header>

      {/* Contenido principal */}
      <main className="mx-auto w-full max-w-7xl flex-1 px-4 py-8 sm:px-6">{children}</main>

      {/* Footer oficial de la Fundación Hacer Futuro */}
      <footer className="mt-auto border-t border-slate-200 bg-white py-6 text-xs text-slate-500">
        <div className="mx-auto flex max-w-7xl flex-col items-center justify-between gap-4 px-4 sm:flex-row sm:px-6">
          <div className="flex items-center gap-3">
            <HacerFuturoLogo className="h-8 w-8" showText={false} />
            <div className="flex flex-col">
              <span className="font-bold text-slate-800">
                Banco de Anteojos · Fundación Hacer Futuro
              </span>
              <span>Disminuyendo la inequidad social a través de la salud visual y el cuidado ambiental</span>
            </div>
          </div>

          <div className="flex flex-wrap items-center justify-center gap-5 font-medium">
            <span>Chacabuco 27, 1° Piso, Tucumán</span>
            <span>WhatsApp: 381-398-2020</span>
            <a
              href="https://www.instagram.com/fundacionhacerfuturo"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-1 font-bold text-orange-600 hover:text-orange-700 hover:underline"
            >
              <span>Instagram: @fundacionhacerfuturo</span>
            </a>
          </div>
        </div>
      </footer>
    </div>
  )
}
