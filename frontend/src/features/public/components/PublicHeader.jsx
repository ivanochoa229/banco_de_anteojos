import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../../context/useAuth'
import { BancoAnteojosLogo, HacerFuturoLogo } from '../../../components/BrandLogo'

const PUBLIC_LINKS = [
  { href: '#como-funciona', label: 'Cómo funciona' },
  { href: '#donar', label: 'Donar' },
  { href: '#solicitar', label: 'Solicitar' },
  { href: '#testimonios', label: 'Testimonios' },
  { href: '#premios', label: 'Premios' },
  { href: '#alianzas', label: 'Alianzas' },
]

export function PublicHeader() {
  const { isAuthenticated, role, logout } = useAuth()
  const [isMobileOpen, setIsMobileOpen] = useState(false)

  const portalRoute =
    role === 'APPLICANT' ? '/solicitante' : role === 'OPERATOR' ? '/operador' : '/admin'

  return (
    <header className="sticky top-0 z-50 border-b border-slate-200 bg-white/95 backdrop-blur-md">
      {/* Barra superior de contacto rápido sin emojis */}
      <div className="bg-slate-900 px-4 py-2 text-[11px] font-medium text-slate-300 sm:px-6">
        <div className="mx-auto flex max-w-7xl flex-wrap items-center justify-between gap-2">
          <div className="flex items-center gap-2">
            <svg className="h-3.5 w-3.5 text-orange-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
              <path strokeLinecap="round" strokeLinejoin="round" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
              <path strokeLinecap="round" strokeLinejoin="round" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
            </svg>
            <span>Chacabuco 27, 1° Piso · San Miguel de Tucumán</span>
          </div>
          <div className="flex items-center gap-4">
            <a
              href="https://wa.me/5493813982020"
              target="_blank"
              rel="noopener noreferrer"
              className="text-orange-400 hover:text-orange-300 transition"
            >
              WhatsApp: 381-398-2020
            </a>
            <span className="hidden sm:inline text-slate-600">|</span>
            <a
              href="https://www.instagram.com/fundacionhacerfuturo"
              target="_blank"
              rel="noopener noreferrer"
              className="hidden text-slate-300 hover:text-white transition sm:inline"
            >
              @fundacionhacerfuturo
            </a>
          </div>
        </div>
      </div>

      {/* Navegación Principal: Limpia y espaciosa */}
      <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-3.5 sm:px-6">
        <Link to="/" className="flex items-center gap-3.5 transition hover:opacity-95">
          <HacerFuturoLogo className="h-10 sm:h-11 w-auto" />
          <div className="h-8 w-[1.5px] bg-slate-200" />
          <BancoAnteojosLogo className="h-12 sm:h-14 w-auto" />
        </Link>

        {/* Links desktop esenciales */}
        <nav className="hidden items-center gap-8 md:flex">
          {PUBLIC_LINKS.map(({ href, label }) => (
            <a
              key={href}
              href={href}
              className="text-sm font-semibold text-slate-700 transition hover:text-orange-600"
            >
              {label}
            </a>
          ))}
        </nav>

        {/* Botón único de acción */}
        <div className="hidden items-center gap-3 sm:flex">
          {isAuthenticated ? (
            <>
              <Link
                to={portalRoute}
                className="rounded-xl bg-orange-600 px-5 py-2.5 text-xs font-bold text-white shadow-sm transition hover:bg-orange-700 active:scale-95"
              >
                Mi Portal →
              </Link>
              <button
                type="button"
                onClick={logout}
                className="rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-xs font-bold text-slate-600 shadow-2xs hover:bg-slate-50 transition"
              >
                Cerrar sesión
              </button>
            </>
          ) : (
            <Link
              to="/login"
              className="rounded-xl bg-orange-600 px-5 py-2.5 text-xs font-bold text-white shadow-sm transition hover:bg-orange-700 active:scale-95"
            >
              Iniciar sesión / Registrarte
            </Link>
          )}
        </div>

        {/* Hamburguesa móvil */}
        <button
          type="button"
          onClick={() => setIsMobileOpen((prev) => !prev)}
          className="rounded-xl border border-slate-200 p-2 text-slate-600 hover:bg-slate-100 md:hidden"
          aria-label="Abrir menú"
        >
          <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
            {isMobileOpen ? (
              <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
            ) : (
              <path strokeLinecap="round" strokeLinejoin="round" d="M4 6h16M4 12h16M4 18h16" />
            )}
          </svg>
        </button>
      </div>

      {/* Menú móvil desplegable */}
      {isMobileOpen && (
        <div className="border-t border-slate-200 bg-white px-4 py-4 md:hidden">
          <nav className="flex flex-col gap-3">
            {PUBLIC_LINKS.map(({ href, label }) => (
              <a
                key={href}
                href={href}
                onClick={() => setIsMobileOpen(false)}
                className="text-sm font-bold text-slate-800 hover:text-orange-600 py-1"
              >
                {label}
              </a>
            ))}
            <div className="mt-3 border-t border-slate-100 pt-3">
              {isAuthenticated ? (
                <div className="flex flex-col gap-2">
                  <Link
                    to={portalRoute}
                    onClick={() => setIsMobileOpen(false)}
                    className="w-full rounded-xl bg-orange-600 py-2.5 text-center text-xs font-bold text-white shadow-sm"
                  >
                    Mi Portal →
                  </Link>
                  <button
                    type="button"
                    onClick={() => {
                      setIsMobileOpen(false)
                      logout()
                    }}
                    className="w-full rounded-xl border border-slate-200 bg-white py-2 text-center text-xs font-bold text-slate-600"
                  >
                    Cerrar sesión
                  </button>
                </div>
              ) : (
                <Link
                  to="/login"
                  onClick={() => setIsMobileOpen(false)}
                  className="block w-full rounded-xl bg-orange-600 py-2.5 text-center text-xs font-bold text-white shadow-sm"
                >
                  Iniciar sesión / Registrarte
                </Link>
              )}
            </div>
          </nav>
        </div>
      )}
    </header>
  )
}
