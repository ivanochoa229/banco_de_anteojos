import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { applicantsApi } from '../../../api/applicantsApi'
import { Alert } from '../../../components/Alert'
import { Layout } from '../../../components/Layout'
import { ApplicantForm } from '../components/ApplicantForm'
import { missingProfileFields } from '../profileCompleteness'

// Autogestión: applicantId sale del JWT en el backend, no se pide acá.
export function MyProfilePage() {
  const queryClient = useQueryClient()

  const profileQuery = useQuery({
    queryKey: ['me', 'profile'],
    queryFn: applicantsApi.getMine,
  })

  const updateMutation = useMutation({
    mutationFn: (data) => applicantsApi.updateMine(data),
    onSuccess: (updated) => queryClient.setQueryData(['me', 'profile'], updated),
  })

  const profile = profileQuery.data
  const missing = profile ? missingProfileFields(profile) : []

  return (
    <Layout>
      <h2 className="text-xl font-semibold text-slate-900">Mi perfil</h2>
      <p className="mt-1 text-sm text-slate-500">
        Revisá tus datos personales y completá los que falten. La fundación los usa para
        contactarte y atenderte.
      </p>

      {profileQuery.isPending && <p className="mt-6 text-slate-500">Cargando tu perfil…</p>}
      {profileQuery.isError && (
        <div className="mt-6">
          <Alert>{profileQuery.error.message}</Alert>
        </div>
      )}

      {profile && (
        <div className="mt-6 grid grid-cols-1 gap-6 lg:grid-cols-3">
          <section className="lg:col-span-2">
            {missing.length > 0 && (
              <div className="mb-4 rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800">
                Te falta completar: <span className="font-semibold">{missing.join(' y ')}</span>.
              </div>
            )}
            {updateMutation.isSuccess && (
              <div
                role="status"
                className="mb-4 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800"
              >
                Tus datos se guardaron correctamente.
              </div>
            )}
            <div className="rounded-lg border border-slate-200 bg-white p-6">
              <ApplicantForm
                initialValues={profile}
                emailReadOnly
                submitLabel="Guardar cambios"
                onSubmit={(data) => updateMutation.mutate(data)}
                isPending={updateMutation.isPending}
                submitError={updateMutation.error?.message}
              />
            </div>
          </section>

          <aside className="rounded-lg border border-slate-200 bg-white p-6">
            <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-500">
              Datos que valida la fundación
            </h3>
            <p className="mt-1 text-xs text-slate-500">
              No se pueden modificar desde acá. Si hay un error, avisanos por WhatsApp o en la sede.
            </p>
            <dl className="mt-4 space-y-3 text-sm">
              <div>
                <dt className="text-slate-500">DNI</dt>
                <dd className="font-semibold text-slate-900">{profile.dni}</dd>
              </div>
              <div>
                <dt className="text-slate-500">CUIL</dt>
                <dd className="font-semibold text-slate-900">
                  {profile.cuil ?? (
                    <span className="font-normal text-slate-500">
                      Pendiente: se completa cuando la fundación valida tu certificación negativa de
                      ANSES.
                    </span>
                  )}
                </dd>
              </div>
              <div>
                <dt className="text-slate-500">Identidad</dt>
                <dd className="font-semibold text-slate-900">
                  {profile.identityValidated ? 'Validada' : 'Pendiente de validación'}
                </dd>
              </div>
            </dl>
          </aside>
        </div>
      )}
    </Layout>
  )
}
