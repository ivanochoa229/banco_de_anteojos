import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { applicantsApi } from '../../../api/applicantsApi'
import { assignmentsApi } from '../../../api/assignmentsApi'
import { framesApi } from '../../../api/framesApi'
import { Alert } from '../../../components/Alert'
import { Layout } from '../../../components/Layout'
import { Select } from '../../../components/Select'
import { formatDateTime } from '../../../lib/dates'
import { AssignmentActions } from '../components/AssignmentActions'
import { AssignmentStageBadge } from '../components/AssignmentStageBadge'

const SCOPE_OPTIONS = [
  { value: 'live', label: 'En curso' },
  { value: 'all', label: 'Todas' },
]

export function AssignmentsQueuePage() {
  const [scope, setScope] = useState('live')
  const queryClient = useQueryClient()

  const assignmentsQuery = useQuery({
    queryKey: ['assignments', scope],
    queryFn: () => assignmentsApi.list(scope === 'live'),
  })

  // La asignación solo trae ids; el nombre y el precinto salen de estas dos listas, que son
  // chicas y quedan cacheadas por el resto de las pantallas.
  const applicantsQuery = useQuery({ queryKey: ['applicants'], queryFn: applicantsApi.list })
  const framesQuery = useQuery({ queryKey: ['frames', ''], queryFn: () => framesApi.list('') })

  const applicantById = Object.fromEntries(
    (applicantsQuery.data ?? []).map((applicant) => [applicant.id, applicant]),
  )
  const frameById = Object.fromEntries((framesQuery.data ?? []).map((frame) => [frame.id, frame]))

  // Cada hito mueve también el estado del marco: hay que refrescar el inventario.
  function invalidateCircuit() {
    queryClient.invalidateQueries({ queryKey: ['assignments'] })
    queryClient.invalidateQueries({ queryKey: ['frames'] })
  }

  const milestoneMutation = useMutation({
    mutationFn: ({ assignmentId, action }) => assignmentsApi[action](assignmentId),
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

  const assignments = assignmentsQuery.data ?? []
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
            onChange={(event) => setScope(event.target.value)}
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
        {assignmentsQuery.isSuccess && assignments.length === 0 && (
          <p className="rounded-lg border border-dashed border-slate-300 bg-white px-6 py-10 text-center text-slate-500">
            {scope === 'live'
              ? 'No hay asignaciones en curso. Se arrancan desde la ficha del solicitante.'
              : 'Todavía no se asignó ningún marco.'}
          </p>
        )}
        {assignments.length > 0 && (
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
                {assignments.map((assignment) => {
                  const applicant = applicantById[assignment.applicantId]
                  const frame = frameById[assignment.frameId]
                  return (
                    <tr key={assignment.id} className="hover:bg-slate-50">
                      <td className="px-4 py-3">
                        <Link
                          to={`/assignments/${assignment.id}`}
                          className="font-medium text-sky-700 hover:underline"
                        >
                          {applicant
                            ? `${applicant.lastName}, ${applicant.firstName}`
                            : `Beneficiario #${assignment.applicantId}`}
                        </Link>
                      </td>
                      <td className="px-4 py-3 text-slate-600">
                        {frame?.sealCode ?? `#${assignment.frameId}`}
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
                          onMilestone={(action) =>
                            milestoneMutation.mutate({ assignmentId: assignment.id, action })
                          }
                          onCancel={(reason) =>
                            cancelMutation.mutate({ assignmentId: assignment.id, reason })
                          }
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
