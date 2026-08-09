import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { applicantsApi } from '../../../api/applicantsApi'
import { Alert } from '../../../components/Alert'
import { Layout } from '../../../components/Layout'
import { formatDate } from '../../../lib/dates'

export function ApplicantsListPage() {
  const { data: applicants, isPending, isError, error } = useQuery({
    queryKey: ['applicants'],
    queryFn: applicantsApi.list,
  })

  return (
    <Layout>
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-semibold text-slate-900">Solicitantes</h2>
        <Link
          to="/applicants/new"
          className="rounded-lg bg-sky-700 px-4 py-2 text-sm font-medium text-white transition hover:bg-sky-800"
        >
          Nuevo solicitante
        </Link>
      </div>

      <div className="mt-6">
        {isPending && <p className="text-slate-500">Cargando solicitantes…</p>}
        {isError && <Alert>{error.message}</Alert>}
        {applicants?.length === 0 && (
          <p className="rounded-lg border border-dashed border-slate-300 bg-white px-6 py-10 text-center text-slate-500">
            No hay solicitantes cargados todavía.
          </p>
        )}
        {applicants?.length > 0 && (
          <div className="overflow-x-auto rounded-lg border border-slate-200 bg-white">
            <table className="w-full text-left text-sm">
              <thead className="border-b border-slate-200 bg-slate-50 text-xs uppercase text-slate-500">
                <tr>
                  <th className="px-4 py-3">Apellido y nombre</th>
                  <th className="px-4 py-3">DNI</th>
                  <th className="px-4 py-3">Nacimiento</th>
                  <th className="px-4 py-3">Contacto</th>
                  <th className="px-4 py-3">Identidad</th>
                  <th className="px-4 py-3"></th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {applicants.map((applicant) => (
                  <tr key={applicant.id} className="hover:bg-slate-50">
                    <td className="px-4 py-3 font-medium text-slate-900">
                      {applicant.lastName}, {applicant.firstName}
                    </td>
                    <td className="px-4 py-3 text-slate-600">{applicant.dni}</td>
                    <td className="px-4 py-3 text-slate-600">{formatDate(applicant.birthDate)}</td>
                    <td className="px-4 py-3 text-slate-600">
                      {applicant.phone ?? applicant.email ?? '—'}
                    </td>
                    <td className="px-4 py-3">
                      {applicant.identityValidated ? (
                        <span className="rounded-full bg-green-50 px-2.5 py-0.5 text-xs font-medium text-green-700">
                          Validada
                        </span>
                      ) : (
                        <span className="rounded-full bg-amber-50 px-2.5 py-0.5 text-xs font-medium text-amber-700">
                          Pendiente
                        </span>
                      )}
                    </td>
                    <td className="px-4 py-3 text-right">
                      <div className="flex justify-end gap-4">
                        <Link
                          to={`/applicants/${applicant.id}/prescriptions`}
                          className="font-medium text-sky-700 hover:underline"
                        >
                          Recetas
                        </Link>
                        <Link
                          to={`/applicants/${applicant.id}/anses-certificate`}
                          className="font-medium text-sky-700 hover:underline"
                        >
                          ANSES
                        </Link>
                        <Link
                          to={`/applicants/${applicant.id}/assignments/new`}
                          className="font-medium text-sky-700 hover:underline"
                        >
                          Asignar marco
                        </Link>
                        <Link
                          to={`/applicants/${applicant.id}/appointments/new`}
                          className="font-medium text-sky-700 hover:underline"
                        >
                          Turno
                        </Link>
                        <Link
                          to={`/applicants/${applicant.id}/edit`}
                          className="font-medium text-sky-700 hover:underline"
                        >
                          Editar
                        </Link>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </Layout>
  )
}
