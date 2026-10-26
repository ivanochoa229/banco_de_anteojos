import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { applicantsApi } from '../../../api/applicantsApi'
import { Alert } from '../../../components/Alert'
import { Layout } from '../../../components/Layout'
import { formatDate } from '../../../lib/dates'
import { ANSES_VALIDITY_DAYS } from '../ansesValidity'
import { AnsesCertificateFileLink } from '../components/AnsesCertificateFileLink'
import { AnsesCertificateUpload } from '../components/AnsesCertificateUpload'
import { AnsesValidityNote } from '../components/AnsesValidityNote'
import { ApplicantForm } from '../components/ApplicantForm'
import { MyPrescriptionsSection } from '../components/MyPrescriptionsSection'
import { joinFields, missingProfileFields } from '../profileCompleteness'

function Field({ label, children }) {
  return (
    <div>
      <dt className="text-slate-500">{label}</dt>
      <dd className="font-semibold text-slate-900">{children}</dd>
    </div>
  )
}

// Autogestión: applicantId sale del JWT en el backend, no se pide acá.
export function MyProfilePage() {
  const queryClient = useQueryClient()

  const profileQuery = useQuery({
    queryKey: ['me', 'profile'],
    queryFn: applicantsApi.getMine,
  })

  // Que no haya certificación es el estado normal de un beneficiario nuevo, no un error.
  const certificateQuery = useQuery({
    queryKey: ['me', 'anses-certificate'],
    queryFn: async () => {
      try {
        return await applicantsApi.getMyAnsesCertificate()
      } catch (error) {
        if (error.status === 404) return null
        throw error
      }
    },
  })

  const updateMutation = useMutation({
    mutationFn: (data) => applicantsApi.updateMine(data),
    onSuccess: (updated) => queryClient.setQueryData(['me', 'profile'], updated),
  })

  // Validarla carga el CUIL en el perfil: hay que volver a pedirlo.
  const uploadMutation = useMutation({
    mutationFn: (file) => applicantsApi.uploadMyAnsesCertificate(file),
    onSuccess: (certificate) => {
      queryClient.setQueryData(['me', 'anses-certificate'], certificate)
      queryClient.invalidateQueries({ queryKey: ['me', 'profile'] })
    },
  })

  const profile = profileQuery.data
  const certificate = certificateQuery.data
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
          <div className="space-y-6 lg:col-span-2">
            {missing.length > 0 && (
              <div className="rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800">
                Te falta completar: <span className="font-semibold">{joinFields(missing)}</span>.
              </div>
            )}

            <section>
              <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-500">
                Datos personales
              </h3>
              {updateMutation.isSuccess && (
                <div
                  role="status"
                  className="mt-3 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800"
                >
                  Tus datos se guardaron correctamente.
                </div>
              )}
              <div className="mt-3 rounded-lg border border-slate-200 bg-white p-6">
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

            <section>
              <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-500">
                Certificación negativa de ANSES
              </h3>
              <p className="mt-1 text-sm text-slate-500">
                Acredita que no tenés cobertura médica. Descargala del sitio de ANSES y subí el PDF
                original, sin imprimir ni escanear. El sistema lee el código de barras, verifica que
                el CUIL corresponda a tu DNI y que no tenga más de {ANSES_VALIDITY_DAYS} días: si
                todo está bien, tu CUIL queda cargado.
              </p>
              <div className="mt-3 rounded-lg border border-slate-200 bg-white p-6">
                {certificateQuery.isError && <Alert>{certificateQuery.error.message}</Alert>}
                {certificate && (
                  <dl className="mb-5 grid grid-cols-1 gap-3 text-sm sm:grid-cols-2">
                    <Field label="CUIL">{certificate.cuil}</Field>
                    <Field label="Fecha de emisión">{formatDate(certificate.issueDate)}</Field>
                    <Field label="Vigencia">
                      <AnsesValidityNote issueDate={certificate.issueDate} />
                    </Field>
                    <Field label="Archivo">
                      <AnsesCertificateFileLink
                        fetchCertificate={applicantsApi.getMyAnsesCertificate}
                        fileName={certificate.fileOriginalName}
                      />
                    </Field>
                  </dl>
                )}
                {uploadMutation.isSuccess && (
                  <div
                    role="status"
                    className="mb-4 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800"
                  >
                    Certificación validada. Tu CUIL quedó cargado.
                  </div>
                )}
                <AnsesCertificateUpload
                  hasCertificate={Boolean(certificate)}
                  onUpload={(file) => uploadMutation.mutate(file)}
                  isPending={uploadMutation.isPending}
                  uploadError={uploadMutation.error?.message}
                />
              </div>
            </section>

            <MyPrescriptionsSection />
          </div>

          <aside className="h-fit rounded-lg border border-slate-200 bg-white p-6">
            <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-500">
              Identidad
            </h3>
            <dl className="mt-4 space-y-3 text-sm">
              <Field label="DNI">{profile.dni}</Field>
              <Field label="Validación del DNI">
                {profile.identityValidated ? (
                  'Validado'
                ) : (
                  <span className="font-normal text-slate-600">
                    Pendiente: se valida en persona. Presentá tu DNI en la sede el día de tu turno.
                  </span>
                )}
              </Field>
              <Field label="CUIL">
                {profile.cuil ?? (
                  <span className="font-normal text-slate-600">
                    Pendiente: se carga al validar tu certificación negativa de ANSES.
                  </span>
                )}
              </Field>
            </dl>
            <p className="mt-4 text-xs text-slate-500">
              El DNI no se puede modificar desde acá. Si hay un error, avisanos por WhatsApp o en la
              sede.
            </p>
          </aside>
        </div>
      )}
    </Layout>
  )
}
