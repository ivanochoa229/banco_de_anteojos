import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { applicantsApi } from '../../../api/applicantsApi'
import { appointmentsApi } from '../../../api/appointmentsApi'
import { Alert } from '../../../components/Alert'
import { Input } from '../../../components/Input'
import { Layout } from '../../../components/Layout'
import { Select } from '../../../components/Select'
import { formatDateTime, todayIsoDate } from '../../../lib/dates'
import { AppointmentActions } from '../components/AppointmentActions'
import { AppointmentReceiptLink } from '../components/AppointmentReceiptLink'
import { AppointmentStatusBadge } from '../components/AppointmentStatusBadge'

const SCOPE_OPTIONS = [
  { value: 'day', label: 'Por día' },
  { value: 'all', label: 'Todos' },
]

export function AppointmentsAgendaPage() {
  const [scope, setScope] = useState('day')
  const [date, setDate] = useState(todayIsoDate())
  const queryClient = useQueryClient()

  const filterDate = scope === 'day' ? date : null

  const appointmentsQuery = useQuery({
    queryKey: ['appointments', filterDate ?? 'all'],
    queryFn: () => appointmentsApi.list(filterDate),
  })

  // El turno solo trae ids; el nombre del beneficiario sale de esta lista, chica y cacheada.
  const applicantsQuery = useQuery({ queryKey: ['applicants'], queryFn: applicantsApi.list })
  const applicantById = Object.fromEntries(
    (applicantsQuery.data ?? []).map((applicant) => [applicant.id, applicant]),
  )

  function invalidateAppointments() {
    queryClient.invalidateQueries({ queryKey: ['appointments'] })
  }

  const rescheduleMutation = useMutation({
    mutationFn: ({ appointmentId, scheduledAt }) =>
      appointmentsApi.reschedule(appointmentId, scheduledAt),
    onSuccess: invalidateAppointments,
  })

  const cancelMutation = useMutation({
    mutationFn: ({ appointmentId, reason }) => appointmentsApi.cancel(appointmentId, reason),
    onSuccess: invalidateAppointments,
  })

  const attendanceMutation = useMutation({
    mutationFn: ({ appointmentId, attended }) =>
      appointmentsApi.registerAttendance(appointmentId, attended),
    onSuccess: invalidateAppointments,
  })

  const approveMutation = useMutation({
    mutationFn: ({ appointmentId }) => appointmentsApi.approve(appointmentId),
    onSuccess: invalidateAppointments,
  })

  function isRowPending(appointmentId) {
    return [rescheduleMutation, cancelMutation, attendanceMutation, approveMutation].some(
      (mutation) => mutation.isPending && mutation.variables.appointmentId === appointmentId,
    )
  }

  const appointments = appointmentsQuery.data ?? []
  const actionError =
    rescheduleMutation.error?.message ??
    cancelMutation.error?.message ??
    attendanceMutation.error?.message ??
    approveMutation.error?.message

  return (
    <Layout>
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-semibold text-slate-900">Turnos</h2>
          <p className="mt-1 text-sm text-slate-500">
            La agenda de atención: aprobá el comprobante subido, reprogramá, cancelá o registrá la
            asistencia del día. Los turnos se agendan desde la ficha del solicitante.
          </p>
        </div>
        <div className="flex items-end gap-3">
          <div className="w-36">
            <Select
              id="scope"
              label="Mostrar"
              options={SCOPE_OPTIONS}
              value={scope}
              onChange={(event) => setScope(event.target.value)}
            />
          </div>
          {scope === 'day' && (
            <div className="w-44">
              <Input
                id="date"
                label="Día"
                type="date"
                value={date}
                onChange={(event) => setDate(event.target.value)}
              />
            </div>
          )}
        </div>
      </div>

      {actionError && (
        <div className="mt-6">
          <Alert>{actionError}</Alert>
        </div>
      )}

      <div className="mt-6">
        {appointmentsQuery.isPending && <p className="text-slate-500">Cargando turnos…</p>}
        {appointmentsQuery.isError && <Alert>{appointmentsQuery.error.message}</Alert>}
        {appointmentsQuery.isSuccess && appointments.length === 0 && (
          <p className="rounded-lg border border-dashed border-slate-300 bg-white px-6 py-10 text-center text-slate-500">
            {scope === 'day'
              ? 'No hay turnos para este día.'
              : 'Todavía no se agendó ningún turno. Se agendan desde la ficha del solicitante.'}
          </p>
        )}
        {appointments.length > 0 && (
          <div className="overflow-x-auto rounded-lg border border-slate-200 bg-white">
            <table className="w-full text-left text-sm">
              <thead className="border-b border-slate-200 bg-slate-50 text-xs uppercase text-slate-500">
                <tr>
                  <th className="px-4 py-3">Fecha y hora</th>
                  <th className="px-4 py-3">Beneficiario</th>
                  <th className="px-4 py-3">Motivo</th>
                  <th className="px-4 py-3">Estado</th>
                  <th className="px-4 py-3">Acciones</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {appointments.map((appointment) => {
                  const applicant = applicantById[appointment.applicantId]
                  return (
                    <tr key={appointment.id} className="hover:bg-slate-50">
                      <td className="px-4 py-3 font-medium text-slate-900">
                        {formatDateTime(appointment.scheduledAt)}
                      </td>
                      <td className="px-4 py-3 text-slate-600">
                        {applicant
                          ? `${applicant.lastName}, ${applicant.firstName}`
                          : `Beneficiario #${appointment.applicantId}`}
                        {appointment.notes && (
                          <p className="mt-0.5 max-w-52 truncate text-xs text-slate-400" title={appointment.notes}>
                            {appointment.notes}
                          </p>
                        )}
                      </td>
                      <td className="px-4 py-3 text-slate-600">
                        {appointment.assignmentId ? (
                          <Link
                            to={`/assignments/${appointment.assignmentId}`}
                            className="font-medium text-sky-700 hover:underline"
                          >
                            Retiro de anteojos
                          </Link>
                        ) : (
                          'Atención general'
                        )}
                      </td>
                      <td className="px-4 py-3">
                        <AppointmentStatusBadge status={appointment.status} />
                        {appointment.receiptOriginalName && (
                          <div className="mt-1">
                            <AppointmentReceiptLink
                              appointmentId={appointment.id}
                              fileName={appointment.receiptOriginalName}
                            />
                          </div>
                        )}
                      </td>
                      <td className="px-4 py-3">
                        <AppointmentActions
                          appointment={appointment}
                          isPending={isRowPending(appointment.id)}
                          onReschedule={(scheduledAt) =>
                            rescheduleMutation.mutate({ appointmentId: appointment.id, scheduledAt })
                          }
                          onCancel={(reason) =>
                            cancelMutation.mutate({ appointmentId: appointment.id, reason })
                          }
                          onAttendance={(attended) =>
                            attendanceMutation.mutate({ appointmentId: appointment.id, attended })
                          }
                          onApprove={() => approveMutation.mutate({ appointmentId: appointment.id })}
                        />
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </Layout>
  )
}
