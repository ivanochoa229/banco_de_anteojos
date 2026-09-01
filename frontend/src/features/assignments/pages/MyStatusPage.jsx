import { useQuery } from '@tanstack/react-query'
import { assignmentsApi } from '../../../api/assignmentsApi'
import { Alert } from '../../../components/Alert'
import { Layout } from '../../../components/Layout'
import { AssignmentStageBadge } from '../components/AssignmentStageBadge'
import { AssignmentTimeline } from '../components/AssignmentTimeline'

// Autogestión, solo lectura: applicantId sale del JWT en el backend. Mover el circuito
// (enviar a la óptica, entregar, cancelar) sigue siendo una acción del operador.
export function MyStatusPage() {
  const assignmentsQuery = useQuery({
    queryKey: ['me', 'assignments'],
    queryFn: assignmentsApi.listMine,
  })

  const assignments = assignmentsQuery.data ?? []

  return (
    <Layout>
      <h2 className="text-xl font-semibold text-slate-900">Estado de mi marco</h2>
      <p className="mt-1 text-sm text-slate-500">
        El recorrido de cada par de anteojos asignado, desde que se elige el marco hasta la entrega.
      </p>

      {assignmentsQuery.isPending && <p className="mt-6 text-slate-500">Cargando…</p>}
      {assignmentsQuery.isError && (
        <div className="mt-6">
          <Alert>{assignmentsQuery.error.message}</Alert>
        </div>
      )}
      {assignmentsQuery.isSuccess && assignments.length === 0 && (
        <p className="mt-6 rounded-lg border border-dashed border-slate-300 bg-white px-6 py-10 text-center text-slate-500">
          Todavía no tenés ningún marco asignado.
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
