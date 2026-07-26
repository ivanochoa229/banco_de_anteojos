import { Link, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { applicantsApi } from '../../../api/applicantsApi'
import { Alert } from '../../../components/Alert'
import { Layout } from '../../../components/Layout'
import { formatDateTime } from '../../../lib/dates'
import { PrescriptionFileCell } from '../components/PrescriptionFileCell'
import { PrescriptionForm } from '../components/PrescriptionForm'

// Las dioptrías se leen siempre con signo y dos decimales (+1.00 / -0.75).
function formatDiopter(value) {
  if (value === null || value === undefined) return '—'
  const number = Number(value)
  return `${number > 0 ? '+' : ''}${number.toFixed(2)}`
}

function formatEye(sphere, cylinder, axis) {
  if (sphere === null && cylinder === null && axis === null) return '—'
  const parts = [`Esf ${formatDiopter(sphere)}`, `Cil ${formatDiopter(cylinder)}`]
  if (axis !== null && axis !== undefined) parts.push(`Eje ${axis}°`)
  return parts.join(' · ')
}

export function ApplicantPrescriptionsPage() {
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

  // La graduación se guarda primero y el archivo va aparte: si falla la subida, la receta
  // igual quedó registrada y el operador puede reintentar el adjunto.
  const createMutation = useMutation({
    mutationFn: async ({ data, file }) => {
      const prescription = await applicantsApi.createPrescription(applicantId, data)
      if (!file) return { fileError: null }
      try {
        await applicantsApi.uploadPrescriptionFile(applicantId, prescription.id, file)
        return { fileError: null }
      } catch (error) {
        // No se propaga: la receta ya existe y reintentar el alta la duplicaría. El operador
        // adjunta el archivo desde el historial.
        return { fileError: error.message }
      }
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ['applicants', applicantId, 'prescriptions'] })
    },
  })

  const applicant = applicantQuery.data
  // El endpoint no garantiza orden; la más reciente va primero.
  const prescriptions = prescriptionsQuery.data
    ? [...prescriptionsQuery.data].sort((a, b) => b.createdAt.localeCompare(a.createdAt))
    : []

  return (
    <Layout>
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-semibold text-slate-900">Recetas</h2>
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

      <section className="mt-6">
        <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-500">Historial</h3>
        <div className="mt-3">
          {prescriptionsQuery.isPending && <p className="text-slate-500">Cargando recetas…</p>}
          {prescriptionsQuery.isError && <Alert>{prescriptionsQuery.error.message}</Alert>}
          {prescriptionsQuery.isSuccess && prescriptions.length === 0 && (
            <p className="rounded-lg border border-dashed border-slate-300 bg-white px-6 py-10 text-center text-slate-500">
              Este solicitante todavía no tiene recetas cargadas.
            </p>
          )}
          {prescriptions.length > 0 && (
            <div className="overflow-x-auto rounded-lg border border-slate-200 bg-white">
              <table className="w-full text-left text-sm">
                <thead className="border-b border-slate-200 bg-slate-50 text-xs uppercase text-slate-500">
                  <tr>
                    <th className="px-4 py-3">Fecha de carga</th>
                    <th className="px-4 py-3">Ojo derecho (OD)</th>
                    <th className="px-4 py-3">Ojo izquierdo (OI)</th>
                    <th className="px-4 py-3">Receta del médico</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {prescriptions.map((prescription) => (
                    <tr key={prescription.id} className="hover:bg-slate-50">
                      <td className="px-4 py-3 text-slate-600">
                        {formatDateTime(prescription.createdAt)}
                      </td>
                      <td className="px-4 py-3 font-medium text-slate-900">
                        {formatEye(
                          prescription.rightSphere,
                          prescription.rightCylinder,
                          prescription.rightAxis,
                        )}
                      </td>
                      <td className="px-4 py-3 font-medium text-slate-900">
                        {formatEye(
                          prescription.leftSphere,
                          prescription.leftCylinder,
                          prescription.leftAxis,
                        )}
                      </td>
                      <td className="px-4 py-3">
                        <PrescriptionFileCell
                          applicantId={applicantId}
                          prescription={prescription}
                        />
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </section>

      <section className="mt-8">
        <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-500">
          Nueva receta
        </h3>
        {createMutation.data?.fileError && (
          <div className="mt-3">
            <Alert>
              La receta se guardó, pero no se pudo subir el archivo ({createMutation.data.fileError}).
              Adjuntalo desde el historial.
            </Alert>
          </div>
        )}
        <div className="mt-3 rounded-lg border border-slate-200 bg-white p-6">
          <PrescriptionForm
            onSubmit={(data, file) => createMutation.mutateAsync({ data, file })}
            isPending={createMutation.isPending}
            submitError={createMutation.error?.message}
          />
        </div>
      </section>
    </Layout>
  )
}
