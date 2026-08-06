import { Link, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { assignmentsApi } from '../../../api/assignmentsApi'
import { Alert } from '../../../components/Alert'
import { Layout } from '../../../components/Layout'
import { formatDate, formatDateTime } from '../../../lib/dates'
import { DONOR_TYPE_LABELS } from '../../donors/labels'
import { FrameStatusBadge } from '../../frames/components/FrameStatusBadge'
import { FRAME_MATERIAL_LABELS, FRAME_TYPE_LABELS } from '../../frames/labels'
import { AssignmentActions } from '../components/AssignmentActions'
import { AssignmentStageBadge } from '../components/AssignmentStageBadge'
import { AssignmentTimeline } from '../components/AssignmentTimeline'

function Card({ title, children }) {
  return (
    <div className="rounded-lg border border-slate-200 bg-white p-5">
      <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-500">{title}</h3>
      <dl className="mt-3 space-y-2 text-sm">{children}</dl>
    </div>
  )
}

function Field({ label, children }) {
  return (
    <div className="flex justify-between gap-4">
      <dt className="text-slate-500">{label}</dt>
      <dd className="text-right font-medium text-slate-900">{children}</dd>
    </div>
  )
}

export function AssignmentDetailPage() {
  const { assignmentId } = useParams()
  const queryClient = useQueryClient()

  const detailQuery = useQuery({
    queryKey: ['assignments', assignmentId],
    queryFn: () => assignmentsApi.get(assignmentId),
  })

  function invalidateCircuit() {
    queryClient.invalidateQueries({ queryKey: ['assignments'] })
    queryClient.invalidateQueries({ queryKey: ['frames'] })
  }

  const milestoneMutation = useMutation({
    mutationFn: (action) => assignmentsApi[action](assignmentId),
    onSuccess: invalidateCircuit,
  })

  const cancelMutation = useMutation({
    mutationFn: (reason) => assignmentsApi.cancel(assignmentId, reason),
    onSuccess: invalidateCircuit,
  })

  if (detailQuery.isPending) {
    return (
      <Layout>
        <p className="text-slate-500">Cargando la asignación…</p>
      </Layout>
    )
  }

  if (detailQuery.isError) {
    return (
      <Layout>
        <Alert>{detailQuery.error.message}</Alert>
      </Layout>
    )
  }

  const { assignment, applicant, frame, donor } = detailQuery.data
  const actionError = milestoneMutation.error?.message ?? cancelMutation.error?.message
  const isPending = milestoneMutation.isPending || cancelMutation.isPending

  return (
    <Layout>
      <div className="flex items-center justify-between">
        <div>
          <div className="flex items-center gap-3">
            <h2 className="text-xl font-semibold text-slate-900">Asignación #{assignment.id}</h2>
            <AssignmentStageBadge assignment={assignment} />
          </div>
          <p className="mt-1 text-sm text-slate-500">
            Marco {frame.sealCode} para {applicant.lastName}, {applicant.firstName}
          </p>
        </div>
        <Link to="/assignments" className="text-sm font-medium text-sky-700 hover:underline">
          Volver a asignaciones
        </Link>
      </div>

      {actionError && (
        <div className="mt-6">
          <Alert>{actionError}</Alert>
        </div>
      )}

      <div className="mt-6">
        <AssignmentActions
          assignment={assignment}
          isPending={isPending}
          onMilestone={(action) => milestoneMutation.mutate(action)}
          onCancel={(reason) => cancelMutation.mutate(reason)}
        />
      </div>

      <div className="mt-8 grid grid-cols-1 gap-4 lg:grid-cols-2">
        <div className="rounded-lg border border-slate-200 bg-white p-5">
          <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-500">
            Recorrido
          </h3>
          <div className="mt-4">
            <AssignmentTimeline assignment={assignment} />
          </div>
          {assignment.notes && (
            <p className="mt-2 border-t border-slate-100 pt-3 text-sm text-slate-600">
              <span className="font-medium text-slate-700">Observaciones:</span> {assignment.notes}
            </p>
          )}
        </div>

        <div className="space-y-4">
          <Card title="Beneficiario">
            <Field label="Nombre">
              {applicant.lastName}, {applicant.firstName}
            </Field>
            <Field label="DNI">{applicant.dni}</Field>
            <Field label="Nacimiento">{formatDate(applicant.birthDate)}</Field>
            <Field label="Contacto">{applicant.phone ?? applicant.email ?? '—'}</Field>
            <Field label="Receta">
              <Link
                to={`/applicants/${applicant.id}/prescriptions`}
                className="text-sky-700 hover:underline"
              >
                #{assignment.prescriptionId}
              </Link>
            </Field>
          </Card>

          <Card title="Marco">
            <Field label="Precinto">{frame.sealCode}</Field>
            <Field label="Tipo">{FRAME_TYPE_LABELS[frame.frameType] ?? frame.frameType}</Field>
            <Field label="Material">{FRAME_MATERIAL_LABELS[frame.material] ?? frame.material}</Field>
            <Field label="Estado en inventario">
              <FrameStatusBadge status={frame.status} />
            </Field>
          </Card>

          <Card title="Donante">
            <Field label="Nombre">
              <Link to={`/donors/${donor.id}/frames`} className="text-sky-700 hover:underline">
                {donor.name}
              </Link>
            </Field>
            <Field label="Tipo">{DONOR_TYPE_LABELS[donor.donorType] ?? donor.donorType}</Field>
            <Field label="Marco recibido">{formatDateTime(frame.receivedAt)}</Field>
          </Card>
        </div>
      </div>
    </Layout>
  )
}
