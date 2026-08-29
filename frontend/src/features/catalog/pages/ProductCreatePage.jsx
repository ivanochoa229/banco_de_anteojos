import { useNavigate } from 'react-router-dom'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { catalogApi } from '../../../api/catalogApi'
import { Layout } from '../../../components/Layout'
import { ProductForm } from '../components/ProductForm'

export function ProductCreatePage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const createMutation = useMutation({
    mutationFn: catalogApi.createProduct,
    onSuccess: (product) => {
      queryClient.invalidateQueries({ queryKey: ['catalog', 'products'] })
      navigate(`/catalog/${product.id}/edit`)
    },
  })

  return (
    <Layout>
      <h2 className="text-xl font-semibold text-slate-900">Nuevo producto</h2>
      <div className="mt-6 rounded-lg border border-slate-200 bg-white p-6">
        <ProductForm
          submitLabel="Cargar producto"
          onSubmit={(data) => createMutation.mutate(data)}
          isPending={createMutation.isPending}
          submitError={createMutation.error?.message}
        />
      </div>
    </Layout>
  )
}
