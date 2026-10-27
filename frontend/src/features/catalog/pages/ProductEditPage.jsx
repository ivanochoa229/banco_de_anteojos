import { useState } from 'react'
import { Link, useLocation, useParams } from 'react-router-dom'
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
  const location = useLocation()
  const queryClient = useQueryClient()
  const [confirmingDiscontinue, setConfirmingDiscontinue] = useState(false)

  const productQuery = useQuery({
    queryKey: ['catalog', 'products', productId],
    queryFn: () => catalogApi.getProduct(productId),
  })

  const saleQuery = useQuery({
    queryKey: ['catalog', 'products', productId, 'sale'],
    queryFn: () => catalogApi.getSale(productId),
    enabled: productQuery.data?.status === 'SOLD',
  })

  function invalidateProduct() {
    queryClient.invalidateQueries({ queryKey: ['catalog', 'products'] })
  }

  const updateMutation = useMutation({
    mutationFn: (data) => catalogApi.updateProduct(productId, data),
    onSuccess: invalidateProduct,
  })

  const uploadImageMutation = useMutation({
    mutationFn: (file) => catalogApi.uploadProductImage(productId, file),
    onSuccess: invalidateProduct,
  })

  const discontinueMutation = useMutation({
    mutationFn: () => catalogApi.discontinueProduct(productId),
    onSuccess: () => {
      invalidateProduct()
      setConfirmingDiscontinue(false)
    },
  })

  const product = productQuery.data
  const sale = saleQuery.data
  // Error de la foto elegida en el alta (ver ProductCreatePage). Deja de mostrarse una vez que se
  // sube otra desde acá.
  const creationImageError = uploadImageMutation.isIdle ? location.state?.imageError : null

  if (productQuery.isPending) {
    return (
      <Layout>
        <p className="text-slate-500">Cargando anteojo…</p>
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
          <p className="mt-1 text-sm text-slate-500">{formatCurrency(product.price)}</p>
        </div>
        <Link to="/catalog" className="text-sm font-medium text-sky-700 hover:underline">
          Volver al catálogo
        </Link>
      </div>

      {isActive(product) ? (
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
      ) : (
        product.description && <p className="mt-4 text-sm text-slate-600">{product.description}</p>
      )}

      <section className="mt-8">
        <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-500">Foto</h3>
        <div className="mt-3 rounded-lg border border-slate-200 bg-white p-6">
          {product.imageUrl ? (
            <img
              src={product.imageUrl}
              alt={product.name}
              className="mb-4 h-40 w-40 rounded-lg border border-slate-200 object-cover"
            />
          ) : (
            !isActive(product) && <p className="text-sm text-slate-500">El anteojo no tiene foto.</p>
          )}
          {creationImageError && (
            <div className="mb-4">
              <Alert>El anteojo se cargó, pero la foto no se pudo subir: {creationImageError}</Alert>
            </div>
          )}
          {isActive(product) && (
            <ProductImageUpload
              hasImage={Boolean(product.imageUrl)}
              onUpload={(file) => uploadImageMutation.mutate(file)}
              isPending={uploadImageMutation.isPending}
              uploadError={uploadImageMutation.error?.message}
            />
          )}
        </div>
      </section>

      {product.status === 'SOLD' && (
        <section className="mt-8">
          <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-500">Venta</h3>
          <div className="mt-3 rounded-lg border border-slate-200 bg-white p-6">
            {saleQuery.isPending && <p className="text-slate-500">Cargando venta…</p>}
            {saleQuery.isError && <Alert>{saleQuery.error.message}</Alert>}
            {sale && (
              <dl className="grid grid-cols-1 gap-4 text-sm sm:grid-cols-2">
                <div>
                  <dt className="text-slate-500">Fecha</dt>
                  <dd className="font-medium text-slate-900">{formatDateTime(sale.soldAt)}</dd>
                </div>
                <div>
                  <dt className="text-slate-500">Precio de venta</dt>
                  <dd className="font-medium text-slate-900">{formatCurrency(sale.price)}</dd>
                </div>
                <div>
                  <dt className="text-slate-500">Comprador</dt>
                  <dd className="font-medium text-slate-900">{sale.buyerName ?? '—'}</dd>
                </div>
                {sale.notes && (
                  <div>
                    <dt className="text-slate-500">Notas</dt>
                    <dd className="font-medium text-slate-900">{sale.notes}</dd>
                  </div>
                )}
              </dl>
            )}
          </div>
        </section>
      )}

      {isActive(product) && (
        <section className="mt-8">
          <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-500">
            Baja del catálogo
          </h3>
          <div className="mt-3 rounded-lg border border-red-200 bg-white p-6">
            <p className="text-sm text-slate-600">
              Dar de baja saca el anteojo del catálogo de forma permanente: no se puede volver a
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
