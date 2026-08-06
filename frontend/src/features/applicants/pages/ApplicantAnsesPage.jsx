import { Link, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { applicantsApi } from '../../../api/applicantsApi'
import { Alert } from '../../../components/Alert'
import { Layout } from '../../../components/Layout'
import { formatDate, formatDateTime } from '../../../lib/dates'
import { ANSES_VALIDITY_DAYS, remainingValidityDays } from '../ansesValidity'
import { AnsesCertificateFileLink } from '../components/AnsesCertificateFileLink'
import { AnsesCertificateUpload } from '../components/AnsesCertificateUpload'

function ValidityNote({ issueDate }) {
  const remaining = remainingValidityDays(issueDate)
  if (remaining < 0) {
    const days = Math.abs(remaining)
    return (
      <span className="text-red-700">
        Vencida hace {days} {days === 1 ? 'día' : 'días'}
      </span>
    )
  }
  if (remaining === 0) return <span className="text-amber-700">Vence hoy</span>
  return (
    <span className={remaining <= 5 ? 'text-amber-700' : 'text-slate-600'}>
      Vence en {remaining} {remaining === 1 ? 'día' : 'días'}
    </span>
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

export function ApplicantAnsesPage() {
  const { applicantId } = useParams()
  const queryClient = useQueryClient()

  const applicantQuery = useQuery({
    queryKey: ['applicants', applicantId],
    queryFn: () => applicantsApi.get(applicantId),
  })

  // Que no haya certificación es el estado normal de un solicitante nuevo, no un error:
  // el 404 se traduce a null y la pantalla muestra el estado vacío.
  const certificateQuery = useQuery({
    queryKey: ['applicants', applicantId, 'anses-certificate'],
    queryFn: async () => {
      try {
        return await applicantsApi.getAnsesCertificate(applicantId)
      } catch (error) {
        if (error.status === 404) return null
        throw error
      }
    },
  })

  // Es el mismo aviso que aparece al asignar un marco: mostrarlo acá evita la sorpresa.
  const eligibilityQuery = useQuery({
    queryKey: ['applicants', applicantId, 'eligibility'],
    queryFn: () => applicantsApi.getEligibility(applicantId),
  })

  const uploadMutation = useMutation({
    mutationFn: (file) => applicantsApi.uploadAnsesCertificate(applicantId, file),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['applicants', applicantId, 'anses-certificate'] })
      queryClient.invalidateQueries({ queryKey: ['applicants', applicantId, 'eligibility'] })
    },
  })

  const applicant = applicantQuery.data
  const certificate = certificateQuery.data
  const eligibility = eligibilityQuery.data

  return (
    <Layout>
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-semibold text-slate-900">Certificación negativa de ANSES</h2>
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

      {eligibility && (
        <div className="mt-6">
          {eligibility.eligible ? (
            <p className="rounded-lg border border-green-200 bg-green-50 px-4 py-3 text-sm text-green-800">
              Acredita que no tiene cobertura médica: puede recibir anteojos.
            </p>
          ) : (
            <p className="rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800">
              {eligibility.warning} Igual se le puede asignar un marco: la decisión final es tuya.
            </p>
          )}
        </div>
      )}

      <section className="mt-8">
        <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-500">
          Certificación cargada
        </h3>
        <div className="mt-3">
          {certificateQuery.isPending && <p className="text-slate-500">Cargando…</p>}
          {certificateQuery.isError && <Alert>{certificateQuery.error.message}</Alert>}
          {certificateQuery.isSuccess && !certificate && (
            <p className="rounded-lg border border-dashed border-slate-300 bg-white px-6 py-10 text-center text-slate-500">
              Todavía no se cargó la negativa de este solicitante. Se descarga del sitio de ANSES y
              se sube el PDF original, sin imprimir ni escanear.
            </p>
          )}
          {certificate && (
            <div className="rounded-lg border border-slate-200 bg-white p-5">
              <dl className="space-y-2 text-sm">
                <Field label="CUIL">{certificate.cuil}</Field>
                <Field label="Nº de transacción">{certificate.transactionNumber}</Field>
                <Field label="Fecha de emisión">{formatDate(certificate.issueDate)}</Field>
                <Field label="Vigencia">
                  <ValidityNote issueDate={certificate.issueDate} />
                </Field>
                <Field label="Cargada el">{formatDateTime(certificate.createdAt)}</Field>
                <Field label="Archivo">
                  <AnsesCertificateFileLink
                    applicantId={applicantId}
                    fileName={certificate.fileOriginalName}
                  />
                </Field>
              </dl>
            </div>
          )}
        </div>
      </section>

      <section className="mt-8">
        <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-500">
          {certificate ? 'Reemplazar' : 'Subir la certificación'}
        </h3>
        <p className="mt-1 text-sm text-slate-500">
          El sistema lee el código de barras del PDF y verifica que el CUIL sea el del solicitante y
          que la emisión no tenga más de {ANSES_VALIDITY_DAYS} días. Si algo no cierra, no se guarda
          nada: pedile otra o validala en persona.
        </p>
        <div className="mt-3 rounded-lg border border-slate-200 bg-white p-6">
          <AnsesCertificateUpload
            hasCertificate={Boolean(certificate)}
            onUpload={(file) => uploadMutation.mutate(file)}
            isPending={uploadMutation.isPending}
            uploadError={uploadMutation.error?.message}
          />
        </div>
      </section>
    </Layout>
  )
}
