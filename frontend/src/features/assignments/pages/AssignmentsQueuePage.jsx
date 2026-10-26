import { Link } from 'react-router-dom'
import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { assignmentsApi } from '../../../api/assignmentsApi'
import { Alert } from '../../../components/Alert'
import { Layout } from '../../../components/Layout'
import { Pagination } from '../../../components/Pagination'
import { Select } from '../../../components/Select'
import { formatDateTime } from '../../../lib/dates'
import { useListParams } from '../../../lib/useListParams'
import { AssignmentActions } from '../components/AssignmentActions'
import { AssignmentStageBadge } from '../components/AssignmentStageBadge'

const SCOPE_OPTIONS = [
  { value: 'live', label: 'En curso' },
  { value: 'all', label: 'Todas' },
]

export function AssignmentsQueuePage() {
  const { scope, page, setFilters, setPage } = useListParams({ scope: 'live' })
  const queryClient = useQueryClient()

  // Cada fila ya trae el nombre del beneficiario y el precinto: no hace falta cruzar listas acá.
  const assignmentsQuery = useQuery({
    queryKey: ['assignments', { scope, page }],
    queryFn: () => assignmentsApi.list({ live: scope === 'live', page }),
    placeholderData: keepPreviousData,
  })

  // Cada hito mueve también el estado del marco: hay que refrescar el inventario.
  function invalidateCircuit() {
    queryClient.invalidateQueries({ queryKey: ['assignments'] })
    queryClient.invalidateQueries({ queryKey: ['frames'] })
  }

  const milestoneMutation = useMutation({
    mutationFn: ({ assignmentId, action, payload }) => assignmentsApi[action](assignmentId, payload),
    onSuccess: invalidateCircuit,
  })

  const cancelMutation = useMutation({
    mutationFn: ({ assignmentId, reason }) => assignmentsApi.cancel(assignmentId, reason),
    onSuccess: invalidateCircuit,
  })

  function isRowPending(assignmentId) {
    return (
      (milestoneMutation.isPending && milestoneMutation.variables.assignmentId === assignmentId) ||
      (cancelMutation.isPending && cancelMutation.variables.assignmentId === assignmentId)
    )
  }

  const items = assignmentsQuery.data?.content ?? []
  const actionError = milestoneMutation.error?.message ?? cancelMutation.error?.message

  return (
    <Layout>
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-semibold text-slate-900">Asignaciones</h2>
          <p className="mt-1 text-sm text-slate-500">
            El recorrido de cada par de anteojos: se asigna un marco, se manda con la receta a la
            óptica y vuelve para entregar.
          </p>
        </div>
        <div className="w-48">
          <Select
            id="scope"
            label="Mostrar"
            options={SCOPE_OPTIONS}
            value={scope}
            onChange={(event) => setFilters({ scope: event.target.value })}
          />
        </div>
      </div>

      {actionError && (
        <div className="mt-6">
          <Alert>{actionError}</Alert>
        </div>
      )}

      <div className="mt-6">
        {assignmentsQuery.isPending && <p className="text-slate-500">Cargando asignaciones…</p>}
        {assignmentsQuery.isError && <Alert>{assignmentsQuery.error.message}</Alert>}
        {assignmentsQuery.data?.totalElements === 0 && (
          <p className="rounded-lg border border-dashed border-slate-300 bg-white px-6 py-10 text-center text-slate-500">
            {scope === 'live'
              ? 'No hay asignaciones en curso. Se arrancan desde la ficha del solicitante.'
              : 'Todavía no se asignó ningún marco.'}
          </p>
        )}
        {items.length > 0 && (
          <div className="overflow-x-auto rounded-lg border border-slate-200 bg-white">
            <table className="w-full text-left text-sm">
              <thead className="border-b border-slate-200 bg-slate-50 text-xs uppercase text-slate-500">
                <tr>
                  <th className="px-4 py-3">Beneficiario</th>
                  <th className="px-4 py-3">Marco</th>
                  <th className="px-4 py-3">Estado</th>
                  <th className="px-4 py-3">Asignado</th>
                  <th className="px-4 py-3">Paso siguiente</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {items.map(({ assignment, applicantName, frameSealCode }) => (
                  <tr key={assignment.id} className="hover:bg-slate-50">
                    <td className="px-4 py-3">
                      <Link
                        to={`/assignments/${assignment.id}`}
                        className="font-medium text-sky-700 hover:underline"
                      >
                        {applicantName ?? `Beneficiario #${assignment.applicantId}`}
                      </Link>
                    </td>
                    <td className="px-4 py-3 text-slate-600">
                      {frameSealCode ?? `#${assignment.frameId}`}
                    </td>
                    <td className="px-4 py-3">
                      <AssignmentStageBadge assignment={assignment} />
                    </td>
                    <td className="px-4 py-3 text-slate-600">
                      {formatDateTime(assignment.assignedAt)}
                    </td>
                    <td className="px-4 py-3">
                      <AssignmentActions
                        assignment={assignment}
                        isPending={isRowPending(assignment.id)}
                        onMilestone={(action, payload) =>
                          milestoneMutation.mutate({ assignmentId: assignment.id, action, payload })
                        }
                        onCancel={(reason) =>
                          cancelMutation.mutate({ assignmentId: assignment.id, reason })
                        }
                      />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        <Pagination pageData={assignmentsQuery.data} onPageChange={setPage} />
      </div>
    </Layout>
  )
}
