import { useNavigate, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { framesApi } from '../../../api/framesApi'
import { Alert } from '../../../components/Alert'
import { Layout } from '../../../components/Layout'
import { FrameForm } from '../../donors/components/FrameForm'

export function FrameEditPage() {
  const { frameId } = useParams()
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const { data: frame, isPending, isError, error } = useQuery({
    queryKey: ['frames', frameId],
    queryFn: () => framesApi.get(frameId),
  })

  const updateMutation = useMutation({
    mutationFn: (data) => framesApi.update(frameId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['frames'] })
      navigate('/frames')
    },
  })

  return (
    <Layout>
      <h2 className="text-xl font-semibold text-slate-900">Editar marco</h2>
      <div className="mt-6 max-w-xl rounded-lg border border-slate-200 bg-white p-6">
        {isPending && <p className="text-slate-500">Cargando marco…</p>}
        {isError && <Alert>{error.message}</Alert>}
        {frame && (
          <FrameForm
            initialValues={frame}
            submitLabel="Guardar cambios"
            onSubmit={(data) => updateMutation.mutateAsync(data)}
            isPending={updateMutation.isPending}
            submitError={updateMutation.error?.message}
          />
        )}
      </div>
    </Layout>
  )
}
