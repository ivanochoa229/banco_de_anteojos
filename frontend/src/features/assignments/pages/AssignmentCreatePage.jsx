import { Link, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { applicantsApi } from '../../../api/applicantsApi'
import { assignmentsApi } from '../../../api/assignmentsApi'
import { framesApi } from '../../../api/framesApi'
import { Alert } from '../../../components/Alert'
import { Layout } from '../../../components/Layout'
import { formatDateTime } from '../../../lib/dates'
import { formatEye } from '../../applicants/prescriptionFormat'
import { FRAME_MATERIAL_LABELS, FRAME_TYPE_LABELS } from '../../frames/labels'
import { AssignmentForm } from '../components/AssignmentForm'
import { AssignmentStageBadge } from '../components/AssignmentStageBadge'

function prescriptionLabel(prescription) {
  const eyes = `OD ${formatEye(
    prescription.rightSphere,
    prescription.rightCylinder,
    prescription.rightAxis,
  )} | OI ${formatEye(prescription.leftSphere, prescription.leftCylinder, prescription.leftAxis)}`
  return `${formatDateTime(prescription.createdAt)} · ${eyes}`
}

function frameLabel(frame) {
  const type = FRAME_TYPE_LABELS[frame.frameType] ?? frame.frameType
  const material = FRAME_MATERIAL_LABELS[frame.material] ?? frame.material
  return `${frame.sealCode} · ${type} · ${material}`
}

export function AssignmentCreatePage() {
  const { applicantId } = useParams()
  const queryClient = useQueryClient()

  const applicantQuery = useQuery({
    queryKey: ['applicants', applicantId],
    queryFn: () => applicantsApi.get(applicantId),
  })

  const prescriptionsQuery = useQuery({
    queryKey: ['applicants', applicantId, 'prescriptions'],
    queryFn: () => applicantsApi.listPrescriptions(applicantId),
  })

  // Solo los marcos disponibles: el backend rechaza cualquier otro, así que no se ofrecen.
  const framesQuery = useQuery({
    queryKey: ['frames', 'AVAILABLE'],
    queryFn: () => framesApi.list('AVAILABLE'),
  })

  const assignmentsQuery = useQuery({
    queryKey: ['applicants', applicantId, 'assignments'],
    queryFn: () => assignmentsApi.listByApplicant(applicantId),
  })

  const createMutation = useMutation({
    mutationFn: (data) => assignmentsApi.create(applicantId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['applicants', applicantId, 'assignments'] })
      queryClient.invalidateQueries({ queryKey: ['assignments'] })
      // El marco pasa de disponible a asignado.
      queryClient.invalidateQueries({ queryKey: ['frames'] })
    },
  })

  const applicant = applicantQuery.data
  const prescriptions = prescriptionsQuery.data ?? []
  const frames = framesQuery.data ?? []
  const assignments = assignmentsQuery.data ?? []
  const created = createMutation.data

  return (
    <Layout>
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-semibold text-slate-900">Asignar un marco</h2>
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

      {assignments.length > 0 && (
        <section className="mt-6">
          <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-500">
            Asignaciones anteriores
          </h3>
          <ul className="mt-3 divide-y divide-slate-100 rounded-lg border border-slate-200 bg-white">
            {assignments.map((assignment) => (
              <li key={assignment.id} className="flex items-center justify-between px-4 py-3 text-sm">
                <Link
                  to={`/assignments/${assignment.id}`}
                  className="font-medium text-sky-700 hover:underline"
                >
                  {formatDateTime(assignment.assignedAt)}
                </Link>
                <AssignmentStageBadge assignment={assignment} />
              </li>
            ))}
          </ul>
        </section>
      )}

      <section className="mt-8">
        {created ? (
          <div className="rounded-lg border border-slate-200 bg-white p-6">
            <h3 className="font-semibold text-slate-900">Marco asignado</h3>
            {created.eligibilityWarning ? (
              <p className="mt-3 rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800">
                {created.eligibilityWarning} La asignación se registró igual: la decisión final es
                tuya.
              </p>
            ) : (
              <p className="mt-2 text-sm text-slate-500">
                El beneficiario tenía la negativa de ANSES vigente al momento de asignarle el marco.
              </p>
            )}
            <div className="mt-4 flex gap-4 text-sm font-medium">
              <Link
                to={`/assignments/${created.assignment.id}`}
                className="text-sky-700 hover:underline"
              >
                Ver la trazabilidad
              </Link>
              <Link to="/assignments" className="text-sky-700 hover:underline">
                Ir a la cola de asignaciones
              </Link>
            </div>
          </div>
        ) : (
          <>
            <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-500">
              Nueva asignación
            </h3>
            <p className="mt-1 text-sm text-slate-500">
              El marco se manda a la óptica junto con la receta elegida: la óptica le coloca los
              cristales según esa graduación.
            </p>
            <div className="mt-3 rounded-lg border border-slate-200 bg-white p-6">
              {(prescriptionsQuery.isPending || framesQuery.isPending) && (
                <p className="text-slate-500">Cargando recetas y marcos…</p>
              )}
              {prescriptionsQuery.isError && <Alert>{prescriptionsQuery.error.message}</Alert>}
              {framesQuery.isError && <Alert>{framesQuery.error.message}</Alert>}

              {prescriptionsQuery.isSuccess && prescriptions.length === 0 && (
                <p className="text-slate-500">
                  Este beneficiario no tiene recetas cargadas.{' '}
                  <Link
                    to={`/applicants/${applicantId}/prescriptions`}
                    className="font-medium text-sky-700 hover:underline"
                  >
                    Cargá la receta primero.
                  </Link>
                </p>
              )}

              {framesQuery.isSuccess && prescriptions.length > 0 && frames.length === 0 && (
                <p className="text-slate-500">
                  No hay marcos disponibles en el inventario.{' '}
                  <Link to="/donors" className="font-medium text-sky-700 hover:underline">
                    Cargá los marcos de una donación.
                  </Link>
                </p>
              )}

              {prescriptions.length > 0 && frames.length > 0 && (
                <AssignmentForm
                  prescriptionOptions={prescriptions.map((prescription) => ({
                    value: prescription.id,
                    label: prescriptionLabel(prescription),
                  }))}
                  frameOptions={frames.map((frame) => ({
                    value: frame.id,
                    label: frameLabel(frame),
                  }))}
                  onSubmit={(data) => createMutation.mutate(data)}
                  isPending={createMutation.isPending}
                  submitError={createMutation.error?.message}
                />
              )}
            </div>
          </>
        )}
      </section>
    </Layout>
  )
}
