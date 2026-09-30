import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { appointmentsApi } from '../../../api/appointmentsApi'
import { Alert } from '../../../components/Alert'
import { Layout } from '../../../components/Layout'
import { useAuth } from '../../../context/useAuth'
import { formatDate, formatTime, formatWeekday } from '../../../lib/dates'
import { AppointmentDayForm } from '../components/AppointmentDayForm'

/**
 * Días en que la sede atiende (RF-20). La fundación no abre todos los viernes: el personal carga
 * cada fecha con su horario y su cupo, y los beneficiarios solo pueden pedir turno en esas fechas.
 */
export function AppointmentDaysPage() {
  const queryClient = useQueryClient()
  const { role } = useAuth()
  const portalPrefix = role === 'ADMIN' ? '/admin' : '/operador'
  // null | id del día que se está editando
  const [editingDayId, setEditingDayId] = useState(null)
  const [deletingDayId, setDeletingDayId] = useState(null)
  // Remonta el formulario de alta tras crear, para que vuelva a quedar vacío.
  const [createFormKey, setCreateFormKey] = useState(0)

  const daysQuery = useQuery({ queryKey: ['appointment-days'], queryFn: appointmentsApi.listDays })

  function invalidateDays() {
    queryClient.invalidateQueries({ queryKey: ['appointment-days'] })
  }

  const createMutation = useMutation({
    mutationFn: appointmentsApi.createDay,
    onSuccess: () => {
      invalidateDays()
      setCreateFormKey((key) => key + 1)
    },
  })

  const updateMutation = useMutation({
    mutationFn: ({ appointmentDayId, data }) => appointmentsApi.updateDay(appointmentDayId, data),
    onSuccess: () => {
      invalidateDays()
      setEditingDayId(null)
    },
  })

  const deleteMutation = useMutation({
    mutationFn: appointmentsApi.deleteDay,
    onSuccess: () => {
      invalidateDays()
      setDeletingDayId(null)
    },
  })

  const days = daysQuery.data ?? []

  return (
    <Layout>
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-semibold text-slate-900">Días de atención</h2>
          <p className="mt-1 text-sm text-slate-500">
            Cargá cada día que la sede atiende, con el horario y la cantidad de turnos. Los
            beneficiarios solo pueden pedir turno en estos días.
          </p>
        </div>
        <Link
          to={`${portalPrefix}/turnos`}
          className="text-sm font-medium text-sky-700 hover:underline"
        >
          Volver a la agenda
        </Link>
      </div>

      <section className="mt-6">
        <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-500">
          Nuevo día de atención
        </h3>
        <div className="mt-3 rounded-lg border border-slate-200 bg-white p-6">
          <AppointmentDayForm
            key={createFormKey}
            onSubmit={(data) => createMutation.mutate(data)}
            isPending={createMutation.isPending}
            submitError={createMutation.error?.message}
            submitLabel="Agregar día"
          />
        </div>
      </section>

      <section className="mt-8">
        <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-500">
          Próximos días
        </h3>

        {deleteMutation.error && (
          <div className="mt-3">
            <Alert>{deleteMutation.error.message}</Alert>
          </div>
        )}

        <div className="mt-3">
          {daysQuery.isPending && <p className="text-slate-500">Cargando días…</p>}
          {daysQuery.isError && <Alert>{daysQuery.error.message}</Alert>}
          {daysQuery.isSuccess && days.length === 0 && (
            <p className="rounded-lg border border-dashed border-slate-300 bg-white px-6 py-10 text-center text-slate-500">
              No hay días de atención cargados. Mientras no haya ninguno, nadie puede pedir turno.
            </p>
          )}
          {days.length > 0 && (
            <ul className="divide-y divide-slate-100 rounded-lg border border-slate-200 bg-white">
              {days.map((day) => (
                <li key={day.id} className="px-4 py-4">
                  {editingDayId === day.id ? (
                    <AppointmentDayForm
                      initialDay={day}
                      isBooked={day.bookedCount > 0}
                      onSubmit={(data) =>
                        updateMutation.mutate({ appointmentDayId: day.id, data })
                      }
                      onCancel={() => {
                        setEditingDayId(null)
                        updateMutation.reset()
                      }}
                      isPending={updateMutation.isPending}
                      submitError={updateMutation.error?.message}
                      submitLabel="Guardar cambios"
                    />
                  ) : (
                    <div className="flex flex-wrap items-center justify-between gap-4 text-sm">
                      <div>
                        <p className="font-semibold text-slate-900">
                          {formatWeekday(day.date)} {formatDate(day.date)}
                        </p>
                        <p className="mt-0.5 text-slate-500">
                          {formatTime(day.startTime)} a {formatTime(day.endTime)} · un turno cada{' '}
                          {day.slotDurationMinutes} min
                        </p>
                      </div>
                      <div className="text-slate-600">
                        <span className="font-semibold text-slate-900">{day.bookedCount}</span> de{' '}
                        {day.slotCount} turnos dados ·{' '}
                        <span
                          className={
                            day.availableCount > 0 ? 'font-semibold text-green-700' : 'font-semibold text-red-700'
                          }
                        >
                          {day.availableCount > 0 ? `${day.availableCount} libres` : 'Completo'}
                        </span>
                      </div>
                      {deletingDayId === day.id ? (
                        <div className="flex items-center gap-3">
                          <span className="text-slate-600">¿Borrar este día?</span>
                          <button
                            type="button"
                            onClick={() => deleteMutation.mutate(day.id)}
                            disabled={deleteMutation.isPending}
                            className="font-medium text-red-700 hover:underline disabled:opacity-60"
                          >
                            {deleteMutation.isPending ? 'Borrando…' : 'Sí, borrar'}
                          </button>
                          <button
                            type="button"
                            onClick={() => setDeletingDayId(null)}
                            className="font-medium text-slate-500 hover:underline"
                          >
                            No
                          </button>
                        </div>
                      ) : (
                        <div className="flex items-center gap-3">
                          <button
                            type="button"
                            onClick={() => {
                              setEditingDayId(day.id)
                              setDeletingDayId(null)
                              updateMutation.reset()
                            }}
                            className="font-medium text-sky-700 hover:underline"
                          >
                            Editar
                          </button>
                          <button
                            type="button"
                            onClick={() => {
                              setDeletingDayId(day.id)
                              deleteMutation.reset()
                            }}
                            className="font-medium text-red-700 hover:underline"
                          >
                            Borrar
                          </button>
                        </div>
                      )}
                    </div>
                  )}
                </li>
              ))}
            </ul>
          )}
        </div>
      </section>
    </Layout>
  )
}
