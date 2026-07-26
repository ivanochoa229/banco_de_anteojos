import { useNavigate } from 'react-router-dom'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { applicantsApi } from '../../../api/applicantsApi'
import { Layout } from '../../../components/Layout'
import { ApplicantForm } from '../components/ApplicantForm'

export function ApplicantCreatePage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const createMutation = useMutation({
    mutationFn: applicantsApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['applicants'] })
      navigate('/applicants')
    },
  })

  return (
    <Layout>
      <h2 className="text-xl font-semibold text-slate-900">Nuevo solicitante</h2>
      <div className="mt-6 rounded-lg border border-slate-200 bg-white p-6">
        <ApplicantForm
          dniEditable
          submitLabel="Crear solicitante"
          onSubmit={(data) => createMutation.mutate(data)}
          isPending={createMutation.isPending}
          submitError={createMutation.error?.message}
        />
      </div>
    </Layout>
  )
}
