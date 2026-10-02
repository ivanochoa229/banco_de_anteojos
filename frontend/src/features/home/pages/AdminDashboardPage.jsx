import { Link } from 'react-router-dom'
import { Layout } from '../../../components/Layout'

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
  catalog: (
    <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
      <path strokeLinecap="round" strokeLinejoin="round" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" />
    </svg>
  ),
  indicators: (
    <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
      <path strokeLinecap="round" strokeLinejoin="round" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
    </svg>
  ),
  tryOn: (
    <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
      <path strokeLinecap="round" strokeLinejoin="round" d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" />
      <circle cx="12" cy="13" r="3" />
    </svg>
  ),
}

const ADMIN_SHORTCUTS = [
  {
    to: '/admin/solicitantes',
    title: 'Gestión de Solicitantes',
    description: 'Padrón de beneficiarios, validaciones RENAPER/ANSES y documentación clínica.',
    iconKey: 'applicants',
  },
  {
    to: '/admin/donantes',
    title: 'Donantes y Aportes',
    description: 'Historial de donantes individuales e institucionales y recepción de armazones.',
    iconKey: 'donors',
  },
  {
    to: '/admin/marcos',
    title: 'Inventario de Marcos',
    description: 'Control de stock físico, precintos, materiales y estado de reacondicionamiento.',
    iconKey: 'frames',
  },
  {
    to: '/admin/asignaciones',
    title: 'Asignaciones y Trazabilidad',
    description: 'Monitoreo del circuito: evaluación social, envío a óptica, calibración y entrega.',
    iconKey: 'assignments',
  },
  {
    to: '/admin/turnos',
    title: 'Agenda de Turnos',
    description: 'Días de atención y su cupo, turnos de entrega, revisión oftalmológica y tasa de asistencia.',
    iconKey: 'appointments',
  },
  {
    to: '/admin/envios',
    title: 'Envíos y Logística',
    description: 'Seguimiento de bultos y encomiendas intersucursales vía empresa de transporte.',
    iconKey: 'shipments',
  },
  {
    to: '/admin/catalogo',
    title: 'Catálogo de Sol (Financiamiento)',
    description: 'Administración de productos, precios y stock de la línea comercial de sol.',
    iconKey: 'catalog',
  },
  {
    to: '/admin/indicadores',
    title: 'Panel Analítico de Impacto',
    description: 'Reportes estadísticos, entregas mensuales, comparativas y métricas socioambientales.',
    iconKey: 'indicators',
  },
  {
    to: '/admin/probador',
    title: 'Probador Virtual',
    description: 'Herramienta de simulación facial de marcos para beneficiarios.',
    iconKey: 'tryOn',
  },
]

export function AdminDashboardPage() {
  return (
    <Layout>
      {/* Banner de Dirección y Administración */}
      <div className="relative mb-8 overflow-hidden rounded-3xl bg-gradient-to-r from-slate-950 via-slate-900 to-orange-950 p-6 sm:p-8 text-white shadow-xl shadow-slate-950/10">
        <div className="relative z-10 max-w-2xl">
          <h2 className="text-2xl font-black tracking-tight sm:text-3xl">
            Supervisión General & Trazabilidad
          </h2>
          <p className="mt-2 text-sm text-slate-300 sm:text-base leading-relaxed">
            Control integral del programa Banco de Anteojos: reportes de impacto social, trazabilidad de donaciones, logística y sustentabilidad financiera.
          </p>
        </div>

        <div className="pointer-events-none absolute -right-10 -bottom-10 h-56 w-56 rounded-full bg-orange-600/15 blur-3xl" />
      </div>



      {/* Grid de Todos los Módulos */}
      <div>
        <h3 className="mb-4 text-base font-bold text-slate-900">Módulos del Sistema</h3>
        <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {ADMIN_SHORTCUTS.map((item) => (
            <Link
              key={item.to}
              to={item.to}
              className="group flex flex-col justify-between rounded-2xl border border-slate-200 bg-white p-6 shadow-2xs transition duration-200 hover:-translate-y-1 hover:border-orange-300 hover:shadow-lg"
            >
              <div>
                <div className="mb-4 flex h-12 w-12 items-center justify-center rounded-xl bg-orange-50 text-orange-600 group-hover:bg-orange-600 group-hover:text-white transition">
                  {ICONS[item.iconKey]}
                </div>
                <h4 className="text-base font-bold text-slate-900 group-hover:text-orange-600 transition">
                  {item.title}
                </h4>
                <p className="mt-2 text-xs text-slate-500 leading-relaxed">{item.description}</p>
              </div>

              <div className="mt-5 flex items-center gap-1 text-xs font-bold text-orange-600 group-hover:translate-x-1 transition">
                <span>Ingresar al módulo</span>
                <span>→</span>
              </div>
            </Link>
          ))}
        </div>
      </div>
    </Layout>
  )
}
