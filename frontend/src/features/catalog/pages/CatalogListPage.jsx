import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { catalogApi } from '../../../api/catalogApi'
import { Alert } from '../../../components/Alert'
import { Button } from '../../../components/Button'
import { Layout } from '../../../components/Layout'
import { Select } from '../../../components/Select'
import { useAuth } from '../../../context/useAuth'
import { formatCurrency } from '../../../lib/currency'
import { ProductStatusBadge } from '../components/ProductStatusBadge'
import { SaleForm } from '../components/SaleForm'
import { isActive } from '../labels'

const SCOPE_OPTIONS = [
  { value: 'active', label: 'Activos' },
  { value: 'all', label: 'Todos' },
]

export function CatalogListPage() {
  const { role } = useAuth()
  const [scope, setScope] = useState('active')
  const [sellingProductId, setSellingProductId] = useState(null)
  const queryClient = useQueryClient()

  const productsQuery = useQuery({
    queryKey: ['catalog', 'products', scope],
    queryFn: () => catalogApi.listProducts(scope === 'active' ? 'ACTIVE' : undefined),
  })

  const sellMutation = useMutation({
    mutationFn: ({ productId, data }) => catalogApi.sellProduct(productId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['catalog', 'products'] })
      setSellingProductId(null)
    },
  })

  const products = productsQuery.data ?? []

  return (
    <Layout>
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-semibold text-slate-900">Catálogo de venta</h2>
          <p className="mt-1 text-sm text-slate-500">
            Anteojos de sol disponibles para la venta al público.
          </p>
        </div>
        <div className="flex items-end gap-3">
          <div className="w-40">
            <Select
              id="scope"
              label="Mostrar"
              options={SCOPE_OPTIONS}
              value={scope}
              onChange={(event) => setScope(event.target.value)}
            />
          </div>
          {role === 'ADMIN' && (
            <Link to="/catalog/new">
              <Button className="px-3 py-2.5 text-sm">Nuevo producto</Button>
            </Link>
          )}
        </div>
      </div>

      <div className="mt-6">
        {productsQuery.isPending && <p className="text-slate-500">Cargando catálogo…</p>}
        {productsQuery.isError && <Alert>{productsQuery.error.message}</Alert>}
        {productsQuery.isSuccess && products.length === 0 && (
          <p className="rounded-lg border border-dashed border-slate-300 bg-white px-6 py-10 text-center text-slate-500">
            {scope === 'active' ? 'No hay productos activos.' : 'Todavía no se cargó ningún producto.'}
          </p>
        )}
        {products.length > 0 && (
          <div className="overflow-x-auto rounded-lg border border-slate-200 bg-white">
            <table className="w-full text-left text-sm">
              <thead className="border-b border-slate-200 bg-slate-50 text-xs uppercase text-slate-500">
                <tr>
                  <th className="px-4 py-3">Producto</th>
                  <th className="px-4 py-3">Precio</th>
                  <th className="px-4 py-3">Stock</th>
                  <th className="px-4 py-3">Estado</th>
                  <th className="px-4 py-3">Acciones</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {products.map((product) => (
                  <tr key={product.id} className="hover:bg-slate-50">
                    <td className="px-4 py-3">
                      <p className="font-medium text-slate-900">{product.name}</p>
                      {product.description && (
                        <p className="text-xs text-slate-500">{product.description}</p>
                      )}
                    </td>
                    <td className="px-4 py-3 text-slate-600">{formatCurrency(product.price)}</td>
                    <td className="px-4 py-3 text-slate-600">{product.stockQuantity}</td>
                    <td className="px-4 py-3">
                      <ProductStatusBadge status={product.status} />
                    </td>
                    <td className="px-4 py-3">
                      {sellingProductId === product.id ? (
                        <SaleForm
                          productId={product.id}
                          maxQuantity={product.stockQuantity}
                          isPending={sellMutation.isPending}
                          submitError={
                            sellMutation.variables?.productId === product.id
                              ? sellMutation.error?.message
                              : null
                          }
                          onSell={(data) =>
                            sellMutation.mutate({ productId: product.id, data })
                          }
                          onCancel={() => setSellingProductId(null)}
                        />
                      ) : (
                        <div className="flex items-center gap-3">
                          {isActive(product) && product.stockQuantity > 0 && (
                            <Button
                              onClick={() => setSellingProductId(product.id)}
                              className="px-3 py-2 text-sm"
                            >
                              Vender
                            </Button>
                          )}
                          {role === 'ADMIN' && (
                            <Link
                              to={`/catalog/${product.id}/edit`}
                              className="text-sm font-medium text-sky-700 hover:underline"
                            >
                              Editar
                            </Link>
                          )}
                        </div>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </Layout>
  )
}
