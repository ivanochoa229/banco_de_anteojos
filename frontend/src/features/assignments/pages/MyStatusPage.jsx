import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { assignmentsApi } from '../../../api/assignmentsApi'
import { Alert } from '../../../components/Alert'
import { Layout } from '../../../components/Layout'
import { formatDateTime } from '../../../lib/dates'
import { AssignmentStageBadge } from '../components/AssignmentStageBadge'
import { AssignmentTimeline } from '../components/AssignmentTimeline'

// Autogestión, solo lectura: applicantId sale del JWT en el backend. Mover el circuito
// (enviar a la óptica, entregar, cancelar) sigue siendo una acción del operador.
export function MyStatusPage() {
  const assignmentsQuery = useQuery({
    queryKey: ['me', 'assignments'],
    queryFn: assignmentsApi.listMine,
  })

  const reservationQuery = useQuery({
    queryKey: ['me', 'reservation'],
    queryFn: assignmentsApi.getMyReservation,
  })
  const reservation = reservationQuery.data

  const assignments = assignmentsQuery.data ?? []

  return (
    <Layout>
      <h2 className="text-xl font-semibold text-slate-900">Estado de mi marco</h2>
      <p className="mt-1 text-sm text-slate-500">
        El recorrido de cada par de anteojos asignado, desde que se elige el marco hasta la entrega.
      </p>

      {reservation && (
        <div className="mt-6 rounded-lg border border-emerald-200 bg-emerald-50 p-5 text-sm">
          <p className="font-semibold text-emerald-900">
            Reservaste el marco {reservation.frame.sealCode}
          </p>
          <p className="mt-1 text-emerald-800">
            Queda apartado para vos hasta el {formatDateTime(reservation.reservation.expiresAt)}.
            Sacá un turno para que te lo asignen en la sede.
          </p>
          <div className="mt-2 flex gap-4 font-medium">
            <Link to="/solicitante/mis-turnos" className="text-sky-700 hover:underline">
              Sacar turno
            </Link>
            <Link to="/solicitante/probador" className="text-sky-700 hover:underline">
              Verlo en el probador
            </Link>
          </div>
        </div>
      )}

      {assignmentsQuery.isPending && <p className="mt-6 text-slate-500">Cargando…</p>}
      {assignmentsQuery.isError && (
        <div className="mt-6">
          <Alert>{assignmentsQuery.error.message}</Alert>
        </div>
      )}
      {assignmentsQuery.isSuccess && assignments.length === 0 && !reservation && (
        <p className="mt-6 rounded-lg border border-dashed border-slate-300 bg-white px-6 py-10 text-center text-slate-500">
          Todavía no tenés ningún marco asignado. Podés elegir y reservar uno en el{' '}
          <Link to="/solicitante/probador" className="font-medium text-sky-700 hover:underline">
            probador virtual
          </Link>
          .
        </p>
      )}

      <div className="mt-6 space-y-4">
        {assignments.map((assignment) => (
          <div key={assignment.id} className="rounded-lg border border-slate-200 bg-white p-5">
            <div className="flex items-center gap-3">
              <h3 className="font-semibold text-slate-900">Asignación #{assignment.id}</h3>
              <AssignmentStageBadge assignment={assignment} />
            </div>
            <div className="mt-4">
              <AssignmentTimeline assignment={assignment} />
            </div>
          </div>
        ))}
      </div>
    </Layout>
  )
}
