import { useNavigate } from 'react-router-dom'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { catalogApi } from '../../../api/catalogApi'
import { Layout } from '../../../components/Layout'
import { ProductForm } from '../components/ProductForm'

export function ProductCreatePage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  // Alta y foto son dos requests. Si la foto falla, el anteojo ya quedó cargado: se sigue a la
  // edición con el error a la vista, para reintentar la foto ahí sin volver a cargar los datos.
  const createMutation = useMutation({
    mutationFn: async ({ data, file }) => {
      const product = await catalogApi.createProduct(data)
      if (!file) return { product }
      try {
        await catalogApi.uploadProductImage(product.id, file)
        return { product }
      } catch (error) {
        return { product, imageError: error.message }
      }
    },
    onSuccess: ({ product, imageError }) => {
      queryClient.invalidateQueries({ queryKey: ['catalog', 'products'] })
      navigate(`/catalog/${product.id}/edit`, { state: { imageError } })
    },
  })

  return (
    <Layout>
      <h2 className="text-xl font-semibold text-slate-900">Nuevo anteojo</h2>
      <div className="mt-6 rounded-lg border border-slate-200 bg-white p-6">
        <ProductForm
          withImage
          submitLabel="Cargar anteojo"
          onSubmit={(data, file) => createMutation.mutate({ data, file })}
          isPending={createMutation.isPending}
          submitError={createMutation.error?.message}
        />
      </div>
    </Layout>
  )
}
