import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { donorsApi } from '../../../api/donorsApi'
import { Alert } from '../../../components/Alert'
import { Layout } from '../../../components/Layout'
import { formatDateTime } from '../../../lib/dates'
import { DONOR_TYPE_LABELS } from '../labels'

export function DonorsListPage() {
  const { data: donors, isPending, isError, error } = useQuery({
    queryKey: ['donors'],
    queryFn: donorsApi.list,
  })

  return (
    <Layout>
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-semibold text-slate-900">Donantes</h2>
        <Link
          to="/donors/new"
          className="rounded-lg bg-sky-700 px-4 py-2 text-sm font-medium text-white transition hover:bg-sky-800"
        >
          Nuevo donante
        </Link>
      </div>

      <div className="mt-6">
        {isPending && <p className="text-slate-500">Cargando donantes…</p>}
        {isError && <Alert>{error.message}</Alert>}
        {donors?.length === 0 && (
          <p className="rounded-lg border border-dashed border-slate-300 bg-white px-6 py-10 text-center text-slate-500">
            No hay donantes cargados todavía.
          </p>
        )}
        {donors?.length > 0 && (
          <div className="overflow-x-auto rounded-lg border border-slate-200 bg-white">
            <table className="w-full text-left text-sm">
              <thead className="border-b border-slate-200 bg-slate-50 text-xs uppercase text-slate-500">
                <tr>
                  <th className="px-4 py-3">Nombre</th>
                  <th className="px-4 py-3">Tipo</th>
                  <th className="px-4 py-3">DNI / CUIT</th>
                  <th className="px-4 py-3">Contacto</th>
                  <th className="px-4 py-3">Alta</th>
                  <th className="px-4 py-3"></th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {donors.map((donor) => (
                  <tr key={donor.id} className="hover:bg-slate-50">
                    <td className="px-4 py-3 font-medium text-slate-900">{donor.name}</td>
                    <td className="px-4 py-3 text-slate-600">
                      {DONOR_TYPE_LABELS[donor.donorType] ?? donor.donorType}
                    </td>
                    <td className="px-4 py-3 text-slate-600">{donor.documentNumber ?? '—'}</td>
                    <td className="px-4 py-3 text-slate-600">
                      {donor.phone ?? donor.email ?? '—'}
                    </td>
                    <td className="px-4 py-3 text-slate-600">{formatDateTime(donor.createdAt)}</td>
                    <td className="px-4 py-3 text-right">
                      <Link
                        to={`/donors/${donor.id}/frames`}
                        className="font-medium text-sky-700 hover:underline"
                      >
                        Marcos donados
                      </Link>
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
