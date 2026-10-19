import { Link } from 'react-router-dom'
import { PublicHeader } from '../components/PublicHeader'
import { PublicFooter } from '../components/PublicFooter'
import { VideoTestimonialsSection } from '../components/VideoTestimonialsSection'
import { AwardsSection } from '../components/AwardsSection'
import { AlliesSection } from '../components/AlliesSection'

export function PublicLandingPage() {
  return (
    <div className="min-h-screen bg-slate-50 text-slate-800 antialiased selection:bg-orange-500 selection:text-white">
      <PublicHeader />

      <main>
        {/* ========================================================================= */}
        {/* 1. HERO PRINCIPAL */}
        {/* ========================================================================= */}
        <section id="inicio" className="relative overflow-hidden bg-white py-16 sm:py-24">
          <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
            <div className="grid grid-cols-1 items-center gap-12 lg:grid-cols-12 lg:gap-8">
              {/* Texto principal */}
              <div className="lg:col-span-6 xl:col-span-7">
                <div className="mb-4 flex items-center gap-2.5">
                  <span className="h-0.5 w-6 rounded-full bg-orange-600" />
                  <span className="text-xs font-bold uppercase tracking-widest text-orange-600">
                    Fundación Hacer Futuro · Iniciativa Ecosocial
                  </span>
                </div>

                <h1 className="text-3xl font-black tracking-tight text-slate-900 sm:text-5xl lg:text-5xl/tight">
                  Una nueva mirada puede{' '}
                  <span className="text-orange-600 underline decoration-orange-300 decoration-wavy underline-offset-4">
                    cambiar un futuro
                  </span>
                  .
                </h1>

                <p className="mt-5 text-base leading-relaxed text-slate-600 sm:text-lg">
                  En el <strong className="text-slate-900 font-bold">Banco de Anteojos</strong> recolectamos
                  y reciclamos armazones en desuso para confeccionar lentes recetados a medida, garantizando el
                  derecho a la salud visual de personas sin cobertura médica en Tucumán.
                </p>

                {/* Llamados a la acción */}
                <div className="mt-8 flex flex-wrap items-center gap-4">
                  <Link
                    to="/login"
                    className="inline-flex items-center justify-center rounded-xl bg-orange-600 px-6 py-3.5 text-sm font-bold text-white shadow-md shadow-orange-600/20 transition-all hover:bg-orange-700 hover:shadow-lg active:scale-95"
                  >
                    Iniciar sesión / Registrarte
                  </Link>
                  <a
                    href="#donar"
                    className="inline-flex items-center justify-center rounded-xl border border-slate-300 bg-white px-6 py-3.5 text-sm font-bold text-slate-800 shadow-2xs transition-all hover:border-orange-300 hover:bg-orange-50/50 hover:text-orange-700 active:scale-95"
                  >
                    Quiero donar armazones
                  </a>
                  <a
                    href="#como-funciona"
                    className="inline-flex items-center gap-1.5 text-sm font-bold text-slate-600 transition hover:text-orange-600"
                  >
                    <span>Conocer el circuito</span>
                    <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                      <path strokeLinecap="round" strokeLinejoin="round" d="M19 14l-7 7m0 0l-7-7m7 7V3" />
                    </svg>
                  </a>
                </div>

                {/* Métricas destacadas en el Hero */}
                <div className="mt-12 grid grid-cols-3 gap-6 border-t border-slate-100 pt-8">
                  <div>
                    <span className="block text-2xl font-black text-slate-900 sm:text-3xl">+1.200</span>
                    <span className="text-xs font-semibold text-slate-500">Personas atendidas</span>
                  </div>
                  <div>
                    <span className="block text-2xl font-black text-orange-600 sm:text-3xl">+850</span>
                    <span className="text-xs font-semibold text-slate-500">Anteojos entregados</span>
                  </div>
                  <div>
                    <span className="block text-2xl font-black text-emerald-600 sm:text-3xl">100%</span>
                    <span className="text-xs font-semibold text-slate-500">Economía circular</span>
                  </div>
                </div>
              </div>

              {/* Fotografía real de actividad */}
              <div className="lg:col-span-6 xl:col-span-5">
                <div className="relative mx-auto max-w-md lg:max-w-none">
                  <div className="relative overflow-hidden rounded-3xl border-4 border-white shadow-2xl shadow-slate-900/10">
                    <img
                      src="/images/voluntarios_banco.jpg"
                      alt="Voluntarios de la Fundación Hacer Futuro realizando control visual a una beneficiaria en Tucumán"
                      className="h-[360px] sm:h-[420px] w-full object-cover object-center"
                    />
                    <div className="absolute inset-0 bg-gradient-to-t from-slate-950/70 via-transparent to-transparent" />
                    <div className="absolute bottom-4 left-4 right-4 text-white">
                      <span className="inline-block rounded-md bg-orange-600 px-2.5 py-0.5 text-[10px] font-bold uppercase tracking-wider text-white">
                        Actividad Comunitaria
                      </span>
                      <p className="mt-1.5 text-xs font-medium text-slate-200">
                        Jornada de salud visual y entrega de anteojos recetados · San Miguel de Tucumán
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* ========================================================================= */}
        {/* 2. SOBRE FUNDACIÓN HACER FUTURO */}
        {/* ========================================================================= */}
        <section id="fundacion" className="border-t border-slate-200 bg-slate-50 py-16 sm:py-24">
          <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
            <div className="mx-auto max-w-3xl text-center">
              <span className="text-xs font-black uppercase tracking-widest text-orange-600">
                Identidad y Propósito
              </span>
              <h2 className="mt-2 text-2xl font-black text-slate-900 sm:text-4xl">
                Fundación Hacer Futuro
              </h2>
              <p className="mt-4 text-sm leading-relaxed text-slate-600 sm:text-base">
                Somos una organización de la sociedad civil de San Miguel de Tucumán dedicada a disminuir la
                inequidad social a través de proyectos de salud visual, gestión educativa de calidad y cuidado del medio ambiente.
              </p>
            </div>

            <div className="mt-12 grid grid-cols-1 gap-8 md:grid-cols-3">
              <div className="rounded-2xl border border-slate-200 bg-white p-7 shadow-xs">
                <div className="mb-4 inline-flex h-12 w-12 items-center justify-center rounded-xl bg-orange-50 text-orange-600">
                  <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                    <circle cx="6" cy="12" r="4" />
                    <circle cx="18" cy="12" r="4" />
                    <path strokeLinecap="round" strokeLinejoin="round" d="M10 12h4M2 12l2-4M22 12l-2-4" />
                  </svg>
                </div>
                <h3 className="text-base font-bold text-slate-900">Salud Visual Inclusiva</h3>
                <p className="mt-2 text-xs leading-relaxed text-slate-600">
                  Ver bien no puede ser un privilegio. Acercamos anteojos de calidad a quienes no pueden afrontar los costos de una óptica privada.
                </p>
              </div>

              <div className="rounded-2xl border border-slate-200 bg-white p-7 shadow-xs">
                <div className="mb-4 inline-flex h-12 w-12 items-center justify-center rounded-xl bg-emerald-50 text-emerald-600">
                  <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                  </svg>
                </div>
                <h3 className="text-base font-bold text-slate-900">Economía Circular</h3>
                <p className="mt-2 text-xs leading-relaxed text-slate-600">
                  Evitamos que miles de marcos terminen en basurales. Los reacondicionamos, sanitizamos y reintegramos al circuito solidario.
                </p>
              </div>

              <div className="rounded-2xl border border-slate-200 bg-white p-7 shadow-xs">
                <div className="mb-4 inline-flex h-12 w-12 items-center justify-center rounded-xl bg-sky-50 text-sky-600">
                  <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                  </svg>
                </div>
                <h3 className="text-base font-bold text-slate-900">Comunidad y Compromiso</h3>
                <p className="mt-2 text-xs leading-relaxed text-slate-600">
                  Trabajamos junto a profesionales oftalmólogos, ópticas colaboradoras y voluntarios comprometidos con el futuro de nuestra provincia.
                </p>
              </div>
            </div>
          </div>
        </section>

        {/* ========================================================================= */}
        {/* 3. QUÉ ES EL BANCO DE ANTEOJOS */}
        {/* ========================================================================= */}
        <section id="banco-anteojos" className="border-t border-slate-200 bg-white py-16 sm:py-24">
          <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
            <div className="grid grid-cols-1 items-center gap-12 lg:grid-cols-2">
              <div>
                <span className="text-xs font-black uppercase tracking-widest text-orange-600">
                  Proyecto Socioambiental
                </span>
                <h2 className="mt-2 text-2xl font-black text-slate-900 sm:text-4xl">
                  ¿Qué es el Banco de Anteojos?
                </h2>
                <p className="mt-4 text-sm leading-relaxed text-slate-600 sm:text-base">
                  Es un sistema integral de trazabilidad que conecta a personas y empresas que donan armazones
                  con solicitantes en situación de vulnerabilidad.
                </p>
                <p className="mt-3 text-sm leading-relaxed text-slate-600 sm:text-base">
                  Cada anteojo donado se clasifica por material, calibre y condición, se le asigna un precinto único
                  y se envía a la óptica para calibrar los cristales específicos que prescribe la receta médica del beneficiario.
                </p>

                <div className="mt-6 flex flex-col gap-3">
                  <div className="flex items-center gap-3">
                    <span className="flex h-5 w-5 items-center justify-center rounded-full bg-orange-100 text-xs font-bold text-orange-700">✓</span>
                    <span className="text-xs font-semibold text-slate-700">Trazabilidad individual: conocé el recorrido de cada marco</span>
                  </div>
                  <div className="flex items-center gap-3">
                    <span className="flex h-5 w-5 items-center justify-center rounded-full bg-orange-100 text-xs font-bold text-orange-700">✓</span>
                    <span className="text-xs font-semibold text-slate-700">Validación de identidad con RENAPER y carencia con ANSES</span>
                  </div>
                  <div className="flex items-center gap-3">
                    <span className="flex h-5 w-5 items-center justify-center rounded-full bg-orange-100 text-xs font-bold text-orange-700">✓</span>
                    <span className="text-xs font-semibold text-slate-700">Probador virtual interactivo para elegir el marco adecuado</span>
                  </div>
                </div>
              </div>

              <div>
                <div className="overflow-hidden rounded-3xl border border-slate-200 shadow-xl">
                  <img
                    src="/images/taller_reciclaje.jpg"
                    alt="Taller de clasificación y reciclaje de marcos de anteojos donados"
                    className="h-80 w-full object-cover sm:h-96"
                  />
                  <div className="bg-slate-900 p-4 text-white">
                    <p className="text-xs font-semibold text-slate-300">
                      Taller del Banco de Anteojos: clasificación de materiales (metal y acetato) antes del envío a calibración.
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* ========================================================================= */}
        {/* 4. CÓMO FUNCIONA EL CIRCUITO DE TRAZABILIDAD */}
        {/* ========================================================================= */}
        <section id="como-funciona" className="border-t border-slate-200 bg-slate-50 py-16 sm:py-24">
          <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
            <div className="mx-auto max-w-3xl text-center">
              <span className="text-xs font-black uppercase tracking-widest text-orange-600">
                Transparencia y Proceso
              </span>
              <h2 className="mt-2 text-2xl font-black text-slate-900 sm:text-4xl">
                El Recorrido de un Anteojo
              </h2>
              <p className="mt-4 text-sm text-slate-600 sm:text-base">
                Desde que alguien dona un armazón hasta que el beneficiario lo recibe terminado y ajustado:
              </p>
            </div>

            <div className="mt-12 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
              <div className="relative rounded-2xl border border-slate-200 bg-white p-6 shadow-xs">
                <span className="inline-block rounded-lg bg-orange-100 px-3 py-1 text-xs font-black text-orange-700">
                  PASO 1
                </span>
                <h3 className="mt-4 text-base font-bold text-slate-900">Donación Solidaria</h3>
                <p className="mt-2 text-xs leading-relaxed text-slate-600">
                  Vecinos e instituciones acercan sus marcos a Chacabuco 27 o urnas habilitadas. Se registra al donante.
                </p>
              </div>

              <div className="relative rounded-2xl border border-slate-200 bg-white p-6 shadow-xs">
                <span className="inline-block rounded-lg bg-orange-100 px-3 py-1 text-xs font-black text-orange-700">
                  PASO 2
                </span>
                <h3 className="mt-4 text-base font-bold text-slate-900">Clasificación e Inventario</h3>
                <p className="mt-2 text-xs leading-relaxed text-slate-600">
                  En el taller se analiza estado, medidas y material. Se le asigna un número de precinto único en sistema.
                </p>
              </div>

              <div className="relative rounded-2xl border border-slate-200 bg-white p-6 shadow-xs">
                <span className="inline-block rounded-lg bg-orange-100 px-3 py-1 text-xs font-black text-orange-700">
                  PASO 3
                </span>
                <h3 className="mt-4 text-base font-bold text-slate-900">Óptica y Cristales</h3>
                <p className="mt-2 text-xs leading-relaxed text-slate-600">
                  Se asigna a la persona según su graduación médica. La óptica colaboradora calibra y monta los cristales.
                </p>
              </div>

              <div className="relative rounded-2xl border border-slate-200 bg-white p-6 shadow-xs">
                <span className="inline-block rounded-lg bg-emerald-100 px-3 py-1 text-xs font-black text-emerald-800">
                  PASO 4
                </span>
                <h3 className="mt-4 text-base font-bold text-slate-900">Turno y Entrega Final</h3>
                <p className="mt-2 text-xs leading-relaxed text-slate-600">
                  El beneficiario recibe notificación por WhatsApp o mail, asiste en su turno agendado y retira sus lentes funcionales.
                </p>
              </div>
            </div>
          </div>
        </section>

        {/* ========================================================================= */}
        {/* 5. SECCIÓN DONAR ARMAZONES */}
        {/* ========================================================================= */}
        <section id="donar" className="border-t border-slate-200 bg-white py-16 sm:py-24">
          <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
            <div className="grid grid-cols-1 items-center gap-12 lg:grid-cols-12">
              <div className="lg:col-span-7">
                <span className="text-xs font-black uppercase tracking-widest text-orange-600">
                  Compromiso Ciudadano
                </span>
                <h2 className="mt-2 text-2xl font-black text-slate-900 sm:text-4xl">
                  ¿Cómo donar armazones?
                </h2>
                <p className="mt-4 text-sm leading-relaxed text-slate-600 sm:text-base">
                  Cualquier anteojo que ya no uses (recetado o de sol, de plástico o metal) puede devolverle la vista y las oportunidades a alguien que lo necesita.
                </p>

                <div className="mt-8 space-y-4">
                  <div className="rounded-xl border border-slate-200 bg-slate-50 p-4">
                    <h4 className="text-xs font-bold text-slate-900">Punto de Entrega</h4>
                    <p className="mt-1 text-xs text-slate-600">
                      Chacabuco 27, 1° Piso, San Miguel de Tucumán. De lunes a viernes de 10:00 a 13:00 hs.
                    </p>
                  </div>

                  <div className="rounded-xl border border-slate-200 bg-slate-50 p-4">
                    <h4 className="text-xs font-bold text-slate-900">Cuidado Ambiental</h4>
                    <p className="mt-1 text-xs text-slate-600">
                      Te pedimos utilizar la menor cantidad posible de envoltorios plásticos al acercar tu donación.
                    </p>
                  </div>

                  <div className="rounded-xl border border-slate-200 bg-slate-50 p-4">
                    <h4 className="text-xs font-bold text-slate-900">Identificación</h4>
                    <p className="mt-1 text-xs text-slate-600">
                      Podés colocar una nota con tu nombre, contacto y redes para que podamos agradecerte y registrar tu aporte en la plataforma.
                    </p>
                  </div>
                </div>
              </div>

              <div className="lg:col-span-5">
                <div className="rounded-3xl border border-orange-200 bg-gradient-to-br from-orange-50 to-orange-100/60 p-8 shadow-sm">
                  <h3 className="text-lg font-black text-orange-900">¿Sos una empresa u óptica?</h3>
                  <p className="mt-2 text-xs leading-relaxed text-orange-800">
                    Sumate como donante institucional o taller colaborador. Ayudanos con donaciones de stock en desuso, calibración o insumos.
                  </p>
                  <a
                    href="https://wa.me/5493813982020?text=Hola,%20quisiera%20colaborar%20con%20el%20Banco%20de%20Anteojos"
                    target="_blank"
                    rel="noopener noreferrer"
                    className="mt-6 inline-flex w-full items-center justify-center rounded-xl bg-orange-600 py-3 text-xs font-bold text-white shadow-sm hover:bg-orange-700 transition"
                  >
                    Contactar por WhatsApp Institucional
                  </a>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* ========================================================================= */}
        {/* 6. SECCIÓN SOLICITAR ANTEOJOS */}
        {/* ========================================================================= */}
        <section id="solicitar" className="border-t border-slate-200 bg-slate-50 py-16 sm:py-24">
          <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
            <div className="mx-auto max-w-3xl text-center">
              <span className="text-xs font-black uppercase tracking-widest text-orange-600">
                Acceso a la Salud Visual
              </span>
              <h2 className="mt-2 text-2xl font-black text-slate-900 sm:text-4xl">
                ¿Necesitás anteojos?
              </h2>
              <p className="mt-4 text-sm text-slate-600 sm:text-base">
                Si no contás con obra social ni cobertura médica, podés solicitar tus lentes a través del programa.
              </p>
            </div>

            <div className="mt-12 grid grid-cols-1 gap-6 sm:grid-cols-3">
              <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-xs text-center">
                <div className="mx-auto mb-3 flex h-10 w-10 items-center justify-center rounded-xl bg-orange-50 text-orange-600">
                  <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M10 6H5a2 2 0 00-2 2v9a2 2 0 002 2h14a2 2 0 002-2V8a2 2 0 00-2-2h-5m-4 0V5a2 2 0 114 0v1m-4 0a2 2 0 104 0m-5 8a2 2 0 100-4 2 2 0 000 4zm0 0c1.306 0 2.417.835 2.83 2M9 14a3.001 3.001 0 00-2.83 2M15 11h3m-3 4h2" />
                  </svg>
                </div>
                <h3 className="text-sm font-bold text-slate-900">1. DNI Argentino</h3>
                <p className="mt-2 text-xs text-slate-600">
                  Fotocopia o foto clara del documento nacional de identidad del beneficiario.
                </p>
              </div>

              <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-xs text-center">
                <div className="mx-auto mb-3 flex h-10 w-10 items-center justify-center rounded-xl bg-orange-50 text-orange-600">
                  <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                  </svg>
                </div>
                <h3 className="text-sm font-bold text-slate-900">2. Receta Médica</h3>
                <p className="mt-2 text-xs text-slate-600">
                  Prescripción de un médico oftalmólogo emitida dentro de los últimos 6 meses con graduación OD y OI.
                </p>
              </div>

              <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-xs text-center">
                <div className="mx-auto mb-3 flex h-10 w-10 items-center justify-center rounded-xl bg-orange-50 text-orange-600">
                  <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                  </svg>
                </div>
                <h3 className="text-sm font-bold text-slate-900">3. Certificación Negativa ANSES</h3>
                <p className="mt-2 text-xs text-slate-600">
                  Comprobante emitido por ANSES que certifique no poseer obra social ni aportes vigentes.
                </p>
              </div>
            </div>

            <div className="mt-10 text-center">
              <Link
                to="/register"
                className="inline-flex items-center justify-center rounded-xl bg-orange-600 px-8 py-3.5 text-sm font-bold text-white shadow-md shadow-orange-600/20 hover:bg-orange-700 transition"
              >
                Comenzar Solicitud en Línea
              </Link>
              <p className="mt-2 text-xs text-slate-500">
                El registro es 100% gratuito. Solo atendemos con turno previo asignado por sistema.
              </p>
            </div>
          </div>
        </section>

        {/* ========================================================================= */}
        {/* TESTIMONIOS (CARRUSEL DE VIDEOS REALES) */}
        {/* ========================================================================= */}
        <VideoTestimonialsSection />

        {/* ========================================================================= */}
        {/* NUESTRO IMPACTO */}
        {/* ========================================================================= */}
        <section id="impacto" className="border-t border-slate-200 bg-slate-900 py-16 sm:py-24 text-white">
          <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
            <div className="mx-auto max-w-3xl text-center">
              <span className="text-xs font-black uppercase tracking-widest text-orange-400">
                Resultados Reales
              </span>
              <h2 className="mt-2 text-2xl font-black sm:text-4xl">
                Nuestro Impacto en Tucumán
              </h2>
              <p className="mt-4 text-sm text-slate-400 sm:text-base">
                Cada cifra representa a una persona que pudo volver a leer, estudiar, trabajar o ver el rostro de sus seres queridos.
              </p>
            </div>

            <div className="mt-12 grid grid-cols-2 gap-6 sm:grid-cols-4">
              <div className="rounded-2xl border border-slate-800 bg-slate-800/60 p-6 text-center">
                <span className="block text-3xl font-black text-orange-400 sm:text-5xl">+1.500</span>
                <span className="mt-2 block text-xs font-semibold text-slate-300">Marcos Donados</span>
              </div>
              <div className="rounded-2xl border border-slate-800 bg-slate-800/60 p-6 text-center">
                <span className="block text-3xl font-black text-white sm:text-5xl">+850</span>
                <span className="mt-2 block text-xs font-semibold text-slate-300">Lentes Entregados</span>
              </div>
              <div className="rounded-2xl border border-slate-800 bg-slate-800/60 p-6 text-center">
                <span className="block text-3xl font-black text-emerald-400 sm:text-5xl">+1.200</span>
                <span className="mt-2 block text-xs font-semibold text-slate-300">Beneficiarios</span>
              </div>
              <div className="rounded-2xl border border-slate-800 bg-slate-800/60 p-6 text-center">
                <span className="block text-3xl font-black text-sky-400 sm:text-5xl">+120 kg</span>
                <span className="mt-2 block text-xs font-semibold text-slate-300">Plástico y Metal Reciclado</span>
              </div>
            </div>
          </div>
        </section>

        {/* ========================================================================= */}
        {/* PREMIOS Y MENCIONES */}
        {/* ========================================================================= */}
        <AwardsSection />

        {/* ========================================================================= */}
        {/* ALIANZAS Y EMPRESAS COLABORADORAS */}
        {/* ========================================================================= */}
        <AlliesSection />

        {/* ========================================================================= */}
        {/* 8. CATÁLOGO SOCIAL DE ANTEOJOS DE SOL */}
        {/* ========================================================================= */}
        <section id="catalogo" className="border-t border-slate-200 bg-white py-16 sm:py-24">
          <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
            <div className="mx-auto max-w-3xl text-center">
              <span className="text-xs font-black uppercase tracking-widest text-orange-600">
                Sustentabilidad Financiera
              </span>
              <h2 className="mt-2 text-2xl font-black text-slate-900 sm:text-4xl">
                Catálogo Social de Anteojos de Sol
              </h2>
              <p className="mt-4 text-sm text-slate-600 sm:text-base">
                Comprando un anteojo de sol de la fundación, financias el armado de anteojos recetados para personas que lo necesitan.
              </p>
            </div>

            <div className="mt-10 rounded-3xl border border-slate-200 bg-slate-50 p-8 text-center sm:p-12">
              <div className="mx-auto max-w-lg">
                <div className="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-2xl bg-orange-100 text-orange-600">
                  <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" />
                  </svg>
                </div>
                <h3 className="text-lg font-bold text-slate-900">Línea de Anteojos Solidarios</h3>
                <p className="mt-2 text-xs text-slate-600 leading-relaxed">
                  Modelos de sol seleccionados con protección UV400 certificada. Ingresá a consultar los modelos disponibles en nuestra sede de Chacabuco 27.
                </p>
                <div className="mt-6">
                  <a
                    href="https://wa.me/5493813982020?text=Hola,%20quisiera%20consultar%20el%20cat%C3%A1logo%20de%20anteojos%20de%20sol"
                    target="_blank"
                    rel="noopener noreferrer"
                    className="inline-flex items-center justify-center rounded-xl bg-slate-900 px-6 py-3 text-xs font-bold text-white hover:bg-slate-800 transition"
                  >
                    Consultar Catálogo por WhatsApp
                  </a>
                </div>
              </div>
            </div>
          </div>
        </section>
      </main>

      <PublicFooter />
    </div>
  )
}
