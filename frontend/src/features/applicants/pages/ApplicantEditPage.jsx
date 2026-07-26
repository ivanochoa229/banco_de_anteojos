import { useNavigate, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { applicantsApi } from '../../../api/applicantsApi'
import { Alert } from '../../../components/Alert'
import { Layout } from '../../../components/Layout'
import { ApplicantForm } from '../components/ApplicantForm'

export function ApplicantEditPage() {
  const { applicantId } = useParams()
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const { data: applicant, isPending, isError, error } = useQuery({
    queryKey: ['applicants', applicantId],
    queryFn: () => applicantsApi.get(applicantId),
  })

  const updateMutation = useMutation({
    mutationFn: (data) => applicantsApi.update(applicantId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['applicants'] })
      navigate('/applicants')
    },
  })

  return (
    <Layout>
      <h2 className="text-xl font-semibold text-slate-900">Editar solicitante</h2>
      <div className="mt-6 rounded-lg border border-slate-200 bg-white p-6">
        {isPending && <p className="text-slate-500">Cargando solicitante…</p>}
        {isError && <Alert>{error.message}</Alert>}
        {applicant && (
          <ApplicantForm
            initialValues={applicant}
            submitLabel="Guardar cambios"
            onSubmit={(data) => updateMutation.mutate(data)}
            isPending={updateMutation.isPending}
            submitError={updateMutation.error?.message}
          />
        )}
      </div>
    </Layout>
  )
}
