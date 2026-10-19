import { Link } from 'react-router-dom'
import { Layout } from '../../../components/Layout'
import { useAuth } from '../../../context/useAuth'

const ICONS = {
  applicants: (
    <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
      <path strokeLinecap="round" strokeLinejoin="round" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
    </svg>
  ),
  donors: (
    <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
      <path strokeLinecap="round" strokeLinejoin="round" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
    </svg>
  ),
  frames: (
    <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
      <circle cx="6" cy="12" r="4" />
      <circle cx="18" cy="12" r="4" />
      <path strokeLinecap="round" strokeLinejoin="round" d="M10 12h4M2 12l2-4M22 12l-2-4" />
    </svg>
  ),
  assignments: (
    <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
      <path strokeLinecap="round" strokeLinejoin="round" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
    </svg>
  ),
  appointments: (
    <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
      <path strokeLinecap="round" strokeLinejoin="round" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
    </svg>
  ),
  shipments: (
    <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
      <path strokeLinecap="round" strokeLinejoin="round" d="M8 17a2 2 0 100-4 2 2 0 000 4zm10 0a2 2 0 100-4 2 2 0 000 4zm-14-5V6a2 2 0 012-2h8a2 2 0 012 2v6h3.586a1 1 0 01.707.293l2.414 2.414a1 1 0 01.293.707V17h-2" />
    </svg>
  ),
  tryOn: (
    <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
      <path strokeLinecap="round" strokeLinejoin="round" d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" />
      <circle cx="12" cy="13" r="3" />
    </svg>
  ),
  catalog: (
    <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
      <path strokeLinecap="round" strokeLinejoin="round" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
    </svg>
  ),
  indicators: (
    <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
      <path strokeLinecap="round" strokeLinejoin="round" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
    </svg>
  ),
  status: (
    <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
      <path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
    </svg>
  ),
}

function HomeCard({ to, title, description, iconKey }) {
  return (
    <Link
      to={to}
      className="group relative flex flex-col justify-between rounded-2xl border border-slate-200/90 bg-white p-6 shadow-xs transition-all duration-200 hover:-translate-y-1 hover:border-orange-300 hover:shadow-lg hover:shadow-orange-950/5"
    >
      <div>
        <div className="mb-4 inline-flex h-12 w-12 items-center justify-center rounded-xl bg-orange-50 text-orange-600 transition group-hover:bg-orange-600 group-hover:text-white">
          {ICONS[iconKey] ?? ICONS.frames}
        </div>
        <h3 className="text-base font-bold text-slate-900 group-hover:text-orange-600 transition">
          {title}
        </h3>
        <p className="mt-1.5 text-xs text-slate-500 leading-relaxed">{description}</p>
      </div>
      <div className="mt-4 flex items-center gap-1 text-xs font-bold text-orange-600 opacity-90 transition group-hover:opacity-100 group-hover:translate-x-1">
        <span>Ingresar</span>
        <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 5l7 7-7 7" />
        </svg>
      </div>
    </Link>
  )
}

const STAFF_CARDS = [
  {
    to: '/applicants',
    title: 'Solicitantes',
    description: 'Alta, edición y listado de beneficiarios con su DNI, datos de contacto y validación de identidad.',
    iconKey: 'applicants',
  },
  {
    to: '/donors',
    title: 'Donantes',
    description: 'Registro de quién donó y carga de los marcos que aportó cada persona o institución.',
    iconKey: 'donors',
  },
  {
    to: '/frames',
    title: 'Inventario',
    description: 'Todos los marcos con su precinto, medidas, estado de stock y alertas de stock crítico.',
    iconKey: 'frames',
  },
  {
    to: '/assignments',
    title: 'Asignaciones y Trazabilidad',
    description: 'Circuito integral: selección, calibración en óptica, colocación de cristales y entrega.',
    iconKey: 'assignments',
  },
  {
    to: '/appointments',
    title: 'Turnos',
    description: 'Agenda de atención del día: control de asistencia, reprogramaciones y cancelaciones.',
    iconKey: 'appointments',
  },
  {
    to: '/shipments',
    title: 'Envíos y Logística',
    description: 'Paquetes de marcos entre sucursales y seguimiento de encomiendas (Vía Cargo).',
    iconKey: 'shipments',
  },
  {
    to: '/catalog',
    title: 'Catálogo de Sol',
    description: 'Gestión de productos y stock comercial para financiar proyectos de la fundación.',
    iconKey: 'catalog',
  },
  {
    to: '/try-on',
    title: 'Probador Virtual',
    description: 'Visualización sobre fotografía para ayudar al beneficiario a elegir su marco ideal.',
    iconKey: 'tryOn',
  },
  {
    to: '/indicators',
    title: 'Panel de Impacto',
    description: 'Métricas de anteojos entregados, marcos donados y beneficiarios atendidos.',
    iconKey: 'indicators',
  },
]

const APPLICANT_CARDS = [
  {
    to: '/my-appointments',
    title: 'Pedir un turno',
    description: 'Agendá un turno de atención en la sede o de retiro si tus anteojos ya están listos.',
    iconKey: 'appointments',
  },
  {
    to: '/my-status',
    title: 'Estado de mi anteojo',
    description: 'Seguí el recorrido en tiempo real: asignado, en la óptica, listo o entregado.',
    iconKey: 'status',
  },
  {
    to: '/try-on',
    title: 'Probador virtual',
    description: 'Probate de forma interactiva los marcos disponibles antes de tu cita en la fundación.',
    iconKey: 'tryOn',
  },
]

export function HomePage() {
  const { role } = useAuth()
  const isApplicant = role === 'APPLICANT'
  const cards = isApplicant ? APPLICANT_CARDS : STAFF_CARDS

  return (
    <Layout>
      {/* Banner de bienvenida estilo Fundación Hacer Futuro */}
      <div className="relative mb-8 overflow-hidden rounded-3xl bg-gradient-to-r from-slate-900 via-slate-800 to-orange-950 p-6 sm:p-10 text-white shadow-xl shadow-slate-950/10">
        <div className="relative z-10 max-w-2xl">
          <h2 className="text-2xl font-black tracking-tight sm:text-3xl">
            {isApplicant ? 'Bienvenido a tu portal de salud visual' : 'Sistema de Gestión y Trazabilidad Ecosocial'}
          </h2>
          <p className="mt-2 text-sm text-slate-300 sm:text-base leading-relaxed">
            {isApplicant
              ? 'Desde acá podés solicitar un turno, seguir el estado de armado de tus anteojos y probarte los marcos disponibles.'
              : 'Gestión integral del circuito de donaciones, validaciones de identidad, asignación con óptica y métricas de impacto socioambiental.'}
          </p>
        </div>

        {/* Detalles decorativos de fondo con tono naranja */}
        <div className="pointer-events-none absolute -right-12 -bottom-12 h-64 w-64 rounded-full bg-orange-600/15 blur-3xl" />
        <div className="pointer-events-none absolute top-0 right-1/4 h-32 w-32 rounded-full bg-orange-500/10 blur-2xl" />
      </div>

      {/* Grid de módulos */}
      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3">
        {cards.map((card) => (
          <HomeCard key={card.to} {...card} />
        ))}
      </div>
    </Layout>
  )
}


