import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { appointmentsApi } from '../../../api/appointmentsApi'
import { assignmentsApi } from '../../../api/assignmentsApi'
import { Alert } from '../../../components/Alert'
import { Layout } from '../../../components/Layout'
import { formatDateTime } from '../../../lib/dates'
import { ASSIGNMENT_STAGE_LABELS, assignmentStage } from '../../assignments/labels'
import { AppointmentForm } from '../components/AppointmentForm'
import { AppointmentReceiptUpload } from '../components/AppointmentReceiptUpload'
import { AppointmentStatusBadge } from '../components/AppointmentStatusBadge'

// Autogestión: applicantId sale del JWT en el backend, no se pide acá.
export function MyAppointmentsPage() {
  const queryClient = useQueryClient()

  const appointmentsQuery = useQuery({
    queryKey: ['me', 'appointments'],
    queryFn: appointmentsApi.listMine,
  })

  const assignmentsQuery = useQuery({
    queryKey: ['me', 'assignments'],
    queryFn: assignmentsApi.listMine,
  })

  const daysQuery = useQuery({
    queryKey: ['me', 'appointment-days'],
    queryFn: appointmentsApi.listBookableDays,
  })

  const createMutation = useMutation({
    mutationFn: (data) => appointmentsApi.createMine(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['me', 'appointments'] })
      queryClient.invalidateQueries({ queryKey: ['me', 'appointment-days'] })
    },
  })

  const appointments = appointmentsQuery.data ?? []
  // Solo las asignaciones en curso pueden esperar un retiro: lo entregado o cancelado ya no.
  const liveAssignments = (assignmentsQuery.data ?? []).filter(
    (assignment) => !assignment.deliveredAt && !assignment.cancelledAt,
  )

  return (
    <Layout>
      <h2 className="text-xl font-semibold text-slate-900">Mis turnos</h2>
      <p className="mt-1 text-sm text-slate-500">
        Pedí un turno de atención, o de retiro si ya tenés un par de anteojos listo. La fundación
        atiende solo algunos días: elegí uno de los disponibles.
      </p>

      <div className="mt-6 grid grid-cols-1 gap-6 lg:grid-cols-2">
        <section>
          <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-500">
            Pedir un turno
          </h3>
          <div className="mt-3 rounded-lg border border-slate-200 bg-white p-6">
            {assignmentsQuery.isError && <Alert>{assignmentsQuery.error.message}</Alert>}
            {daysQuery.isError && <Alert>{daysQuery.error.message}</Alert>}
            <AppointmentForm
              days={daysQuery.data ?? []}
              isLoadingDays={daysQuery.isPending}
              assignmentOptions={liveAssignments.map((assignment) => ({
                value: assignment.id,
                label: `${formatDateTime(assignment.assignedAt)} · ${
                  ASSIGNMENT_STAGE_LABELS[assignmentStage(assignment)]
                }`,
              }))}
              onSubmit={(data) => createMutation.mutate(data)}
              isPending={createMutation.isPending}
              submitError={createMutation.error?.message}
            />
            {createMutation.isSuccess && (
              <p className="mt-4 text-sm text-green-700">
                Turno reservado para el {formatDateTime(createMutation.data.scheduledAt)}. Subí el
                comprobante del bono contribución para confirmarlo.
              </p>
            )}
          </div>
        </section>

        <section>
          <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-500">
            Mis turnos
          </h3>
          {appointmentsQuery.isPending && <p className="mt-3 text-slate-500">Cargando…</p>}
          {appointmentsQuery.isError && (
            <div className="mt-3">
              <Alert>{appointmentsQuery.error.message}</Alert>
            </div>
          )}
          {appointmentsQuery.isSuccess && appointments.length === 0 && (
            <p className="mt-3 rounded-lg border border-dashed border-slate-300 bg-white px-4 py-6 text-center text-sm text-slate-500">
              Todavía no tenés turnos.
            </p>
          )}
          {appointments.length > 0 && (
            <ul className="mt-3 divide-y divide-slate-100 rounded-lg border border-slate-200 bg-white">
              {appointments.map((appointment) => (
                <li key={appointment.id} className="px-4 py-3 text-sm">
                  <div className="flex items-center justify-between">
                    <span className="font-medium text-slate-900">
                      {formatDateTime(appointment.scheduledAt)}
                    </span>
                    <AppointmentStatusBadge status={appointment.status} />
                  </div>
                  {appointment.status === 'PENDING_PAYMENT' && (
                    <AppointmentReceiptUpload appointmentId={appointment.id} />
                  )}
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>
    </Layout>
  )
}
