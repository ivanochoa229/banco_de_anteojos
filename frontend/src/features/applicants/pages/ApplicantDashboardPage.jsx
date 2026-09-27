import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { assignmentsApi } from '../../../api/assignmentsApi'
import { appointmentsApi } from '../../../api/appointmentsApi'
import { Layout } from '../../../components/Layout'
import { AssignmentStageBadge } from '../../assignments/components/AssignmentStageBadge'
import { AppointmentStatusBadge } from '../../appointments/components/AppointmentStatusBadge'

export function ApplicantDashboardPage() {
  const assignmentsQuery = useQuery({
    queryKey: ['me', 'assignments'],
    queryFn: assignmentsApi.listMine,
  })

  const appointmentsQuery = useQuery({
    queryKey: ['me', 'appointments'],
    queryFn: appointmentsApi.listMine,
  })

  const assignments = assignmentsQuery.data ?? []
  const appointments = appointmentsQuery.data ?? []
  const latestAssignment = assignments.length > 0 ? assignments[0] : null
  const upcomingAppointment = appointments.find(
    (a) => a.status === 'SCHEDULED' || a.status === 'RESCHEDULED',
  )

  return (
    <Layout>
      {/* Banner de Bienvenida */}
      <div className="relative mb-8 overflow-hidden rounded-3xl bg-gradient-to-r from-slate-900 via-slate-800 to-orange-950 p-6 sm:p-8 text-white shadow-xl shadow-slate-950/10">
        <div className="relative z-10 max-w-2xl">
          <h2 className="text-2xl font-black tracking-tight sm:text-3xl">
            ¡Hola! Te damos la bienvenida a tu portal
          </h2>
          <p className="mt-2 text-sm text-slate-300 sm:text-base leading-relaxed">
            Desde este espacio podés consultar el estado de armado de tus anteojos, gestionar tus turnos de atención y probarte marcos disponibles.
          </p>
        </div>

        <div className="pointer-events-none absolute -right-10 -bottom-10 h-56 w-56 rounded-full bg-orange-600/15 blur-3xl" />
      </div>

      {/* Resumen Rápido de Estado */}
      <div className="mb-8 grid grid-cols-1 gap-6 md:grid-cols-2">
        {/* Tarjeta Estado del Anteojo */}
        <div className="rounded-2xl border border-slate-200/90 bg-white p-6 shadow-xs">
          <div className="flex items-center justify-between">
            <h3 className="text-base font-bold text-slate-900">Estado de tu Anteojo</h3>
            <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-orange-50 text-orange-600">
              <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                <circle cx="6" cy="12" r="4" />
                <circle cx="18" cy="12" r="4" />
                <path strokeLinecap="round" strokeLinejoin="round" d="M10 12h4M2 12l2-4M22 12l-2-4" />
              </svg>
            </div>
          </div>
          <div className="mt-4">
            {latestAssignment ? (
              <div>
                <div className="flex items-center gap-3">
                  <span className="text-sm font-semibold text-slate-700">
                    Asignación #{latestAssignment.id}
                  </span>
                  <AssignmentStageBadge assignment={latestAssignment} />
                </div>
                <p className="mt-2 text-xs text-slate-500">
                  Marco asignado: Precinto #{latestAssignment.frameId}
                </p>
                <div className="mt-4">
                  <Link
                    to="/solicitante/mi-marco"
                    className="inline-flex items-center gap-1 text-xs font-bold text-orange-600 hover:text-orange-700 hover:underline"
                  >
                    <span>Ver recorrido completo del marco</span>
                    <span>→</span>
                  </Link>
                </div>
              </div>
            ) : (
              <div>
                <p className="text-xs text-slate-500">
                  Aún no tenés ningún marco asignado en proceso de taller u óptica.
                </p>
                <p className="mt-2 text-xs text-slate-600 font-medium">
                  Si ya presentaste tu receta y certificación de ANSES, el equipo de la fundación evaluará tu solicitud a la brevedad.
                </p>
              </div>
            )}
          </div>
        </div>

        {/* Tarjeta Próximo Turno */}
        <div className="rounded-2xl border border-slate-200/90 bg-white p-6 shadow-xs">
          <div className="flex items-center justify-between">
            <h3 className="text-base font-bold text-slate-900">Tu Turno de Atención</h3>
            <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-orange-50 text-orange-600">
              <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                <path strokeLinecap="round" strokeLinejoin="round" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
            </div>
          </div>
          <div className="mt-4">
            {upcomingAppointment ? (
              <div>
                <div className="flex items-center gap-3">
                  <span className="text-sm font-semibold text-slate-700">
                    {upcomingAppointment.date} a las {upcomingAppointment.time?.substring(0, 5)} hs
                  </span>
                  <AppointmentStatusBadge status={upcomingAppointment.status} />
                </div>
                <p className="mt-2 text-xs text-slate-500">
                  Sede: Chacabuco 27, 1° Piso · San Miguel de Tucumán
                </p>
                <div className="mt-4">
                  <Link
                    to="/solicitante/mis-turnos"
                    className="inline-flex items-center gap-1 text-xs font-bold text-orange-600 hover:text-orange-700 hover:underline"
                  >
                    <span>Ver o reprogramar turno</span>
                    <span>→</span>
                  </Link>
                </div>
              </div>
            ) : (
              <div>
                <p className="text-xs text-slate-500">
                  No tenés ningún turno pendiente agendado actualmente.
                </p>
                <div className="mt-4">
                  <Link
                    to="/solicitante/mis-turnos"
                    className="inline-flex items-center justify-center rounded-xl bg-orange-600 px-4 py-2 text-xs font-bold text-white shadow-xs hover:bg-orange-700 transition"
                  >
                    Agendar un nuevo turno
                  </Link>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Accesos directos a herramientas del solicitante */}
      <div>
        <h3 className="mb-4 text-base font-bold text-slate-900">Acciones Disponibles</h3>
        <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
          <Link
            to="/solicitante/mis-turnos"
            className="group flex flex-col justify-between rounded-2xl border border-slate-200 bg-white p-5 shadow-2xs transition hover:-translate-y-1 hover:border-orange-300 hover:shadow-md"
          >
            <div>
              <div className="mb-3 flex h-10 w-10 items-center justify-center rounded-xl bg-orange-50 text-orange-600 group-hover:bg-orange-600 group-hover:text-white transition">
                <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
              </div>
              <h4 className="text-sm font-bold text-slate-900 group-hover:text-orange-600 transition">
                Mis Turnos
              </h4>
              <p className="mt-1 text-xs text-slate-500 leading-relaxed">
                Revisá tus turnos agendados o solicitá una nueva fecha de atención.
              </p>
            </div>
            <span className="mt-4 text-xs font-bold text-orange-600 group-hover:translate-x-1 transition inline-flex items-center gap-1">
              Ingresar →
            </span>
          </Link>

          <Link
            to="/solicitante/mi-marco"
            className="group flex flex-col justify-between rounded-2xl border border-slate-200 bg-white p-5 shadow-2xs transition hover:-translate-y-1 hover:border-orange-300 hover:shadow-md"
          >
            <div>
              <div className="mb-3 flex h-10 w-10 items-center justify-center rounded-xl bg-orange-50 text-orange-600 group-hover:bg-orange-600 group-hover:text-white transition">
                <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
              </div>
              <h4 className="text-sm font-bold text-slate-900 group-hover:text-orange-600 transition">
                Estado de mi Marco
              </h4>
              <p className="mt-1 text-xs text-slate-500 leading-relaxed">
                Seguí en vivo si tus anteojos están en calibración o listos para entrega.
              </p>
            </div>
            <span className="mt-4 text-xs font-bold text-orange-600 group-hover:translate-x-1 transition inline-flex items-center gap-1">
              Ingresar →
            </span>
          </Link>

          <Link
            to="/solicitante/probador"
            className="group flex flex-col justify-between rounded-2xl border border-slate-200 bg-white p-5 shadow-2xs transition hover:-translate-y-1 hover:border-orange-300 hover:shadow-md"
          >
            <div>
              <div className="mb-3 flex h-10 w-10 items-center justify-center rounded-xl bg-orange-50 text-orange-600 group-hover:bg-orange-600 group-hover:text-white transition">
                <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" />
                  <circle cx="12" cy="13" r="3" />
                </svg>
              </div>
              <h4 className="text-sm font-bold text-slate-900 group-hover:text-orange-600 transition">
                Probador Virtual
              </h4>
              <p className="mt-1 text-xs text-slate-500 leading-relaxed">
                Subí una foto o activá tu cámara para probarte los marcos disponibles.
              </p>
            </div>
            <span className="mt-4 text-xs font-bold text-orange-600 group-hover:translate-x-1 transition inline-flex items-center gap-1">
              Ingresar →
            </span>
          </Link>

          <Link
            to="/solicitante/catalogo"
            className="group flex flex-col justify-between rounded-2xl border border-slate-200 bg-white p-5 shadow-2xs transition hover:-translate-y-1 hover:border-orange-300 hover:shadow-md"
          >
            <div>
              <div className="mb-3 flex h-10 w-10 items-center justify-center rounded-xl bg-orange-50 text-orange-600 group-hover:bg-orange-600 group-hover:text-white transition">
                <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" />
                </svg>
              </div>
              <h4 className="text-sm font-bold text-slate-900 group-hover:text-orange-600 transition">
                Catálogo de Sol
              </h4>
              <p className="mt-1 text-xs text-slate-500 leading-relaxed">
                Mirá los modelos de anteojos de sol que financian nuestro programa solidario.
              </p>
            </div>
            <span className="mt-4 text-xs font-bold text-orange-600 group-hover:translate-x-1 transition inline-flex items-center gap-1">
              Ingresar →
            </span>
          </Link>
        </div>
      </div>

      {/* Tarjeta de Asistencia y Contacto */}
      <div className="mt-8 rounded-2xl border border-orange-200 bg-orange-50/70 p-6">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h4 className="text-sm font-bold text-orange-950">¿Tenés dudas o necesitás asistencia?</h4>
            <p className="mt-1 text-xs text-orange-800">
              Nuestro equipo de voluntarios y trabajadores sociales está a tu disposición en la sede o vía WhatsApp.
            </p>
          </div>
          <a
            href="https://wa.me/5493813982020?text=Hola,%20tengo%20una%20consulta%20sobre%20mi%20solicitud%20del%20Banco%20de%20Anteojos"
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center justify-center rounded-xl bg-orange-600 px-5 py-2.5 text-xs font-bold text-white shadow-xs hover:bg-orange-700 transition"
          >
            WhatsApp: 381-398-2020
          </a>
        </div>
      </div>
    </Layout>
  )
}
