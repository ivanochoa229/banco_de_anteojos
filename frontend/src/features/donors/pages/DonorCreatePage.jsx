import { useNavigate } from 'react-router-dom'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { donorsApi } from '../../../api/donorsApi'
import { Layout } from '../../../components/Layout'
import { DonorForm } from '../components/DonorForm'

export function DonorCreatePage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const createMutation = useMutation({
    mutationFn: donorsApi.create,
    // Se cae directo en la carga de marcos: el donante se registra porque trajo una donación.
    onSuccess: (donor) => {
      queryClient.invalidateQueries({ queryKey: ['donors'] })
      navigate(`/donors/${donor.id}/frames`)
    },
  })

  return (
    <Layout>
      <h2 className="text-xl font-semibold text-slate-900">Nuevo donante</h2>
      <div className="mt-6 rounded-lg border border-slate-200 bg-white p-6">
        <DonorForm
          onSubmit={(data) => createMutation.mutate(data)}
          isPending={createMutation.isPending}
          submitError={createMutation.error?.message}
        />
      </div>
    </Layout>
  )
}
