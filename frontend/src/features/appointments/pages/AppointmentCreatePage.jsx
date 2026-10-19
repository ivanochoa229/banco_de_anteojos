import { Link, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { applicantsApi } from '../../../api/applicantsApi'
import { appointmentsApi } from '../../../api/appointmentsApi'
import { assignmentsApi } from '../../../api/assignmentsApi'
import { Alert } from '../../../components/Alert'
import { Layout } from '../../../components/Layout'
import { formatDateTime } from '../../../lib/dates'
import { ASSIGNMENT_STAGE_LABELS, assignmentStage } from '../../assignments/labels'
import { AppointmentForm } from '../components/AppointmentForm'
import { AppointmentStatusBadge } from '../components/AppointmentStatusBadge'

export function AppointmentCreatePage() {
  const { applicantId } = useParams()
  const queryClient = useQueryClient()

  const applicantQuery = useQuery({
    queryKey: ['applicants', applicantId],
    queryFn: () => applicantsApi.get(applicantId),
  })

  const appointmentsQuery = useQuery({
    queryKey: ['applicants', applicantId, 'appointments'],
    queryFn: () => appointmentsApi.listByApplicant(applicantId),
  })

  const assignmentsQuery = useQuery({
    queryKey: ['applicants', applicantId, 'assignments'],
    queryFn: () => assignmentsApi.listByApplicant(applicantId),
  })

  const daysQuery = useQuery({ queryKey: ['appointment-days'], queryFn: appointmentsApi.listDays })

  const createMutation = useMutation({
    mutationFn: (data) => appointmentsApi.create(applicantId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['applicants', applicantId, 'appointments'] })
      queryClient.invalidateQueries({ queryKey: ['appointments'] })
      // El cupo del día cambió.
      queryClient.invalidateQueries({ queryKey: ['appointment-days'] })
    },
  })

  const applicant = applicantQuery.data
  const appointments = appointmentsQuery.data ?? []
  // Solo las asignaciones en curso pueden esperar un retiro: lo entregado o cancelado ya no.
  const liveAssignments = (assignmentsQuery.data ?? []).filter(
    (assignment) => !assignment.deliveredAt && !assignment.cancelledAt,
  )
  const created = createMutation.data

  return (
    <Layout>
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-semibold text-slate-900">Agendar un turno</h2>
          {applicant && (
            <p className="mt-1 text-sm text-slate-500">
              {applicant.lastName}, {applicant.firstName} · DNI {applicant.dni}
            </p>
          )}
        </div>
        <Link to="/applicants" className="text-sm font-medium text-sky-700 hover:underline">
          Volver a solicitantes
        </Link>
      </div>

      {applicantQuery.isError && (
        <div className="mt-6">
          <Alert>{applicantQuery.error.message}</Alert>
        </div>
      )}

      {appointments.length > 0 && (
        <section className="mt-6">
          <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-500">
            Turnos anteriores
          </h3>
          <ul className="mt-3 divide-y divide-slate-100 rounded-lg border border-slate-200 bg-white">
            {appointments.map((appointment) => (
              <li key={appointment.id} className="flex items-center justify-between px-4 py-3 text-sm">
                <span className="font-medium text-slate-900">
                  {formatDateTime(appointment.scheduledAt)}
                </span>
                <AppointmentStatusBadge status={appointment.status} />
              </li>
            ))}
          </ul>
        </section>
      )}

      <section className="mt-8">
        {created ? (
          <div className="rounded-lg border border-slate-200 bg-white p-6">
            <h3 className="font-semibold text-slate-900">Turno agendado</h3>
            <p className="mt-2 text-sm text-slate-500">
              {formatDateTime(created.scheduledAt)}
              {applicant?.email
                ? ` · Se le envió la confirmación por email a ${applicant.email}.`
                : ' · El beneficiario no tiene email cargado: avisale por teléfono.'}
            </p>
            <div className="mt-4 flex gap-4 text-sm font-medium">
              <Link to="/appointments" className="text-sky-700 hover:underline">
                Ir a la agenda
              </Link>
              <Link to="/applicants" className="text-sky-700 hover:underline">
                Volver a solicitantes
              </Link>
            </div>
          </div>
        ) : (
          <>
            <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-500">
              Nuevo turno
            </h3>
            <p className="mt-1 text-sm text-slate-500">
              Si el beneficiario viene a buscar un par terminado, elegí la asignación para que el
              turno quede atado a ese retiro.
            </p>
            <div className="mt-3 rounded-lg border border-slate-200 bg-white p-6">
              {daysQuery.isError && <Alert>{daysQuery.error.message}</Alert>}
              <AppointmentForm
                days={(daysQuery.data ?? []).filter((day) => day.availableCount > 0)}
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
            </div>
          </>
        )}
      </section>
    </Layout>
  )
}
