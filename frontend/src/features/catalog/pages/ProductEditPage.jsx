import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { catalogApi } from '../../../api/catalogApi'
import { Alert } from '../../../components/Alert'
import { Button } from '../../../components/Button'
import { Layout } from '../../../components/Layout'
import { formatCurrency } from '../../../lib/currency'
import { formatDateTime } from '../../../lib/dates'
import { ProductForm } from '../components/ProductForm'
import { ProductImageUpload } from '../components/ProductImageUpload'
import { ProductStatusBadge } from '../components/ProductStatusBadge'
import { isActive } from '../labels'

export function ProductEditPage() {
  const { productId } = useParams()
  const queryClient = useQueryClient()
  const [confirmingDiscontinue, setConfirmingDiscontinue] = useState(false)

  const productQuery = useQuery({
    queryKey: ['catalog', 'products', productId],
    queryFn: () => catalogApi.getProduct(productId),
  })

  const imageQuery = useQuery({
    queryKey: ['catalog', 'products', productId, 'image'],
    queryFn: () => catalogApi.getProductImage(productId),
    enabled: Boolean(productQuery.data?.imageOriginalName),
  })

  const salesQuery = useQuery({
    queryKey: ['catalog', 'products', productId, 'sales'],
    queryFn: () => catalogApi.listSales(productId),
  })

  function invalidateProduct() {
    queryClient.invalidateQueries({ queryKey: ['catalog', 'products', productId] })
    queryClient.invalidateQueries({ queryKey: ['catalog', 'products'] })
  }

  const updateMutation = useMutation({
    mutationFn: (data) => catalogApi.updateProduct(productId, data),
    onSuccess: invalidateProduct,
  })

  const uploadImageMutation = useMutation({
    mutationFn: (file) => catalogApi.uploadProductImage(productId, file),
    onSuccess: () => {
      invalidateProduct()
      queryClient.invalidateQueries({ queryKey: ['catalog', 'products', productId, 'image'] })
    },
  })

  const discontinueMutation = useMutation({
    mutationFn: () => catalogApi.discontinueProduct(productId),
    onSuccess: () => {
      invalidateProduct()
      setConfirmingDiscontinue(false)
    },
  })

  const product = productQuery.data
  const sales = salesQuery.data ?? []

  if (productQuery.isPending) {
    return (
      <Layout>
        <p className="text-slate-500">Cargando producto…</p>
      </Layout>
    )
  }

  if (productQuery.isError) {
    return (
      <Layout>
        <Alert>{productQuery.error.message}</Alert>
      </Layout>
    )
  }

  return (
    <Layout>
      <div className="flex items-center justify-between">
        <div>
          <div className="flex items-center gap-3">
            <h2 className="text-xl font-semibold text-slate-900">{product.name}</h2>
            <ProductStatusBadge status={product.status} />
          </div>
          <p className="mt-1 text-sm text-slate-500">
            {formatCurrency(product.price)} · {product.stockQuantity} en stock
          </p>
        </div>
        <Link to="/catalog" className="text-sm font-medium text-sky-700 hover:underline">
          Volver al catálogo
        </Link>
      </div>

      <section className="mt-6">
        <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-500">Datos</h3>
        <div className="mt-3 rounded-lg border border-slate-200 bg-white p-6">
          <ProductForm
            initialValues={product}
            submitLabel="Guardar cambios"
            onSubmit={(data) => updateMutation.mutate(data)}
            isPending={updateMutation.isPending}
            submitError={updateMutation.error?.message}
          />
        </div>
      </section>

      <section className="mt-8">
        <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-500">Foto</h3>
        <div className="mt-3 rounded-lg border border-slate-200 bg-white p-6">
          {imageQuery.data && (
            <img
              src={imageQuery.data.url}
              alt={product.name}
              className="mb-4 h-40 w-40 rounded-lg border border-slate-200 object-cover"
            />
          )}
          <ProductImageUpload
            hasImage={Boolean(product.imageOriginalName)}
            onUpload={(file) => uploadImageMutation.mutate(file)}
            isPending={uploadImageMutation.isPending}
            uploadError={uploadImageMutation.error?.message}
          />
        </div>
      </section>

      <section className="mt-8">
        <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-500">
          Historial de ventas
        </h3>
        <div className="mt-3">
          {salesQuery.isPending && <p className="text-slate-500">Cargando ventas…</p>}
          {salesQuery.isError && <Alert>{salesQuery.error.message}</Alert>}
          {salesQuery.isSuccess && sales.length === 0 && (
            <p className="rounded-lg border border-dashed border-slate-300 bg-white px-6 py-10 text-center text-slate-500">
              Todavía no se vendió ninguna unidad.
            </p>
          )}
          {sales.length > 0 && (
            <div className="overflow-x-auto rounded-lg border border-slate-200 bg-white">
              <table className="w-full text-left text-sm">
                <thead className="border-b border-slate-200 bg-slate-50 text-xs uppercase text-slate-500">
                  <tr>
                    <th className="px-4 py-3">Fecha</th>
                    <th className="px-4 py-3">Cantidad</th>
                    <th className="px-4 py-3">Total</th>
                    <th className="px-4 py-3">Comprador</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {sales.map((sale) => (
                    <tr key={sale.id}>
                      <td className="px-4 py-3 text-slate-600">{formatDateTime(sale.soldAt)}</td>
                      <td className="px-4 py-3 text-slate-600">{sale.quantity}</td>
                      <td className="px-4 py-3 text-slate-600">{formatCurrency(sale.totalAmount)}</td>
                      <td className="px-4 py-3 text-slate-600">{sale.buyerName ?? '—'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </section>

      {isActive(product) && (
        <section className="mt-8">
          <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-500">
            Baja del catálogo
          </h3>
          <div className="mt-3 rounded-lg border border-red-200 bg-white p-6">
            <p className="text-sm text-slate-600">
              Dar de baja saca el producto del catálogo de forma permanente: no se puede volver a
              activar.
            </p>
            {discontinueMutation.error && (
              <div className="mt-3">
                <Alert>{discontinueMutation.error.message}</Alert>
              </div>
            )}
            <div className="mt-4">
              {confirmingDiscontinue ? (
                <div className="flex items-center gap-3">
                  <Button
                    onClick={() => discontinueMutation.mutate()}
                    disabled={discontinueMutation.isPending}
                    className="bg-red-700 px-3 py-2 text-sm hover:bg-red-800"
                  >
                    {discontinueMutation.isPending ? 'Dando de baja…' : 'Confirmar baja'}
                  </Button>
                  <button
                    type="button"
                    onClick={() => setConfirmingDiscontinue(false)}
                    className="px-2 py-2 text-sm font-medium text-slate-500 hover:underline"
                  >
                    Volver
                  </button>
                </div>
              ) : (
                <Button
                  onClick={() => setConfirmingDiscontinue(true)}
                  className="bg-red-700 px-3 py-2 text-sm hover:bg-red-800"
                >
                  Dar de baja
                </Button>
              )}
            </div>
          </div>
        </section>
      )}
    </Layout>
  )
}
