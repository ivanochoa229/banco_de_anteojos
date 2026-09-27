import { Link } from 'react-router-dom'
import { BancoAnteojosLogo, HacerFuturoLogo } from '../../../components/BrandLogo'

export function PublicFooter() {
  return (
    <footer className="border-t border-slate-200 bg-slate-900 text-slate-300">
      <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:py-16">
        <div className="grid grid-cols-1 gap-10 md:grid-cols-2 lg:grid-cols-4">
          {/* Columna 1: Identidad y Misión */}
          <div className="space-y-4">
            <div className="flex items-center gap-3">
              <div className="rounded-xl bg-white p-1.5 shadow-sm">
                <HacerFuturoLogo className="h-9 w-auto" />
              </div>
              <div className="h-8 w-[1.5px] bg-slate-700" />
              <div className="rounded-xl bg-white p-1.5 shadow-sm">
                <BancoAnteojosLogo className="h-10 w-auto" />
              </div>
            </div>
            <h3 className="text-base font-black tracking-tight text-white">
              BANCO DE ANTEOJOS
            </h3>
            <p className="text-xs leading-relaxed text-slate-400">
              Iniciativa socioambiental de la <strong className="text-slate-200">Fundación Hacer Futuro</strong>.
              Reciclamos armazones en desuso y brindamos salud visual a personas sin cobertura social en Tucumán.
            </p>
          </div>

          {/* Columna 2: Navegación Institucional */}
          <div>
            <h4 className="text-xs font-black uppercase tracking-wider text-orange-400">
              El Programa
            </h4>
            <ul className="mt-4 space-y-2.5 text-xs font-medium">
              <li>
                <a href="#fundacion" className="hover:text-white transition">
                  Sobre Fundación Hacer Futuro
                </a>
              </li>
              <li>
                <a href="#banco-anteojos" className="hover:text-white transition">
                  ¿Qué es el Banco de Anteojos?
                </a>
              </li>
              <li>
                <a href="#como-funciona" className="hover:text-white transition">
                  Circuito de Trazabilidad
                </a>
              </li>
              <li>
                <a href="#donar" className="hover:text-white transition">
                  Cómo Donar Armazones
                </a>
              </li>
              <li>
                <a href="#solicitar" className="hover:text-white transition">
                  Requisitos para Solicitantes
                </a>
              </li>
              <li>
                <a href="#testimonios" className="hover:text-white transition">
                  Testimonios en Video
                </a>
              </li>
              <li>
                <a href="#impacto" className="hover:text-white transition">
                  Métricas de Impacto Ecosocial
                </a>
              </li>
              <li>
                <a href="#premios" className="hover:text-white transition">
                  Premios y Menciones
                </a>
              </li>
              <li>
                <a href="#alianzas" className="hover:text-white transition">
                  Empresas y Alianzas
                </a>
              </li>
            </ul>
          </div>

          {/* Columna 3: Autenticación */}
          <div>
            <h4 className="text-xs font-black uppercase tracking-wider text-orange-400">
              Acceso a la Plataforma
            </h4>
            <ul className="mt-4 space-y-2.5 text-xs font-medium">
              <li>
                <Link to="/login" className="hover:text-white transition">
                  Iniciar sesión
                </Link>
              </li>
              <li>
                <Link to="/register" className="hover:text-white transition">
                  Registrarse como solicitante
                </Link>
              </li>
              <li>
                <a href="#catalogo" className="hover:text-white transition">
                  Catálogo de Sol Solidario
                </a>
              </li>
            </ul>
          </div>

          {/* Columna 4: Contacto y Ubicación (Sin emojis) */}
          <div className="space-y-3 text-xs">
            <h4 className="text-xs font-black uppercase tracking-wider text-orange-400">
              Contacto y Sede
            </h4>
            <p className="text-slate-300">
              <span className="font-semibold text-white block">Sede Operativa:</span>
              Chacabuco 27, 1° Piso, San Miguel de Tucumán
            </p>
            <p className="text-slate-300">
              <span className="font-semibold text-white block">Horario de Atención:</span>
              Lunes a Viernes de 10:00 a 13:00 hs (con turno previo)
            </p>
            <p>
              <span className="font-semibold text-white block">WhatsApp Institucional:</span>
              <a
                href="https://wa.me/5493813982020"
                target="_blank"
                rel="noopener noreferrer"
                className="text-orange-400 hover:underline font-bold"
              >
                381-398-2020
              </a>
            </p>
            <p>
              <span className="font-semibold text-white block">Redes Sociales:</span>
              <a
                href="https://www.instagram.com/fundacionhacerfuturo"
                target="_blank"
                rel="noopener noreferrer"
                className="text-slate-300 hover:text-white hover:underline"
              >
                Instagram: @fundacionhacerfuturo
              </a>
            </p>
          </div>
        </div>

        {/* Barra inferior */}
        <div className="mt-12 flex flex-col items-center justify-between gap-4 border-t border-slate-800 pt-8 text-xs text-slate-500 sm:flex-row">
          <p>© {new Date().getFullYear()} Banco de Anteojos · Fundación Hacer Futuro. Todos los derechos reservados.</p>
          <p>Proyecto de Economía Circular y Salud Visual Comunitaria</p>
        </div>
      </div>
    </footer>
  )
}
