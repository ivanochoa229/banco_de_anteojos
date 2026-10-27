import { useState } from 'react'
import { Link } from 'react-router-dom'
import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { catalogApi } from '../../../api/catalogApi'
import { Alert } from '../../../components/Alert'
import { Button } from '../../../components/Button'
import { Layout } from '../../../components/Layout'
import { Pagination } from '../../../components/Pagination'
import { Select } from '../../../components/Select'
import { useAuth } from '../../../context/useAuth'
import { formatCurrency } from '../../../lib/currency'
import { useListParams } from '../../../lib/useListParams'
import { ProductStatusBadge } from '../components/ProductStatusBadge'
import { SaleForm } from '../components/SaleForm'
import { isActive } from '../labels'

const SCOPE_OPTIONS = [
  { value: 'active', label: 'A la venta' },
  { value: 'sold', label: 'Vendidos' },
  { value: 'all', label: 'Todos' },
]

const SCOPE_STATUS = { active: 'ACTIVE', sold: 'SOLD', all: undefined }

const EMPTY_MESSAGES = {
  active: 'No hay anteojos a la venta.',
  sold: 'Todavía no se vendió ningún anteojo.',
  all: 'Todavía no se cargó ningún anteojo.',
}

export function CatalogListPage() {
  const { role } = useAuth()
  // El solicitante solo mira lo que está a la venta: registrar ventas y ver lo vendido es del
  // personal de la fundación.
  const isStaff = role === 'ADMIN' || role === 'OPERATOR'
  const listParams = useListParams({ scope: 'active' })
  const { page, setFilters, setPage } = listParams
  const scope = isStaff && listParams.scope in SCOPE_STATUS ? listParams.scope : 'active'
  const [sellingProductId, setSellingProductId] = useState(null)
  const queryClient = useQueryClient()

  const productsQuery = useQuery({
    queryKey: ['catalog', 'products', { scope, page }],
    queryFn: () => catalogApi.listProducts({ status: SCOPE_STATUS[scope], page }),
    placeholderData: keepPreviousData,
  })

  const sellMutation = useMutation({
    mutationFn: ({ productId, data }) => catalogApi.sellProduct(productId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['catalog', 'products'] })
      setSellingProductId(null)
    },
  })

  const products = productsQuery.data?.content ?? []

  return (
    <Layout>
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-semibold text-slate-900">Catálogo de anteojos de sol</h2>
          <p className="mt-1 text-sm text-slate-500">
            Cada anteojo es único: lo que se recauda financia los anteojos recetados de la fundación.
          </p>
        </div>
        <div className="flex items-end gap-3">
          {isStaff && (
            <div className="w-40">
              <Select
                id="scope"
                label="Mostrar"
                options={SCOPE_OPTIONS}
                value={scope}
                onChange={(event) => setFilters({ scope: event.target.value })}
              />
            </div>
          )}
          {role === 'ADMIN' && (
            <Link to="/catalog/new">
              <Button className="px-3 py-2.5 text-sm">Nuevo anteojo</Button>
            </Link>
          )}
        </div>
      </div>

      <div className="mt-6">
        {productsQuery.isPending && <p className="text-slate-500">Cargando catálogo…</p>}
        {productsQuery.isError && <Alert>{productsQuery.error.message}</Alert>}
        {productsQuery.data?.totalElements === 0 && (
          <p className="rounded-lg border border-dashed border-slate-300 bg-white px-6 py-10 text-center text-slate-500">
            {EMPTY_MESSAGES[scope]}
          </p>
        )}
        {products.length > 0 && (
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {products.map((product) => (
              <article
                key={product.id}
                className="flex flex-col overflow-hidden rounded-lg border border-slate-200 bg-white"
              >
                {product.imageUrl ? (
                  <img
                    src={product.imageUrl}
                    alt={product.name}
                    className="aspect-square w-full bg-slate-50 object-cover"
                  />
                ) : (
                  <div className="flex aspect-square w-full items-center justify-center bg-slate-50 text-sm text-slate-400">
                    Sin foto
                  </div>
                )}
                <div className="flex flex-1 flex-col gap-3 p-4">
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <p className="font-medium text-slate-900">{product.name}</p>
                      {product.description && (
                        <p className="mt-0.5 text-xs text-slate-500">{product.description}</p>
                      )}
                    </div>
                    <ProductStatusBadge status={product.status} />
                  </div>
                  <p className="text-lg font-semibold text-slate-900">{formatCurrency(product.price)}</p>

                  <div className="mt-auto">
                    {sellingProductId === product.id ? (
                      <SaleForm
                        productId={product.id}
                        isPending={sellMutation.isPending}
                        submitError={
                          sellMutation.variables?.productId === product.id
                            ? sellMutation.error?.message
                            : null
                        }
                        onSell={(data) => sellMutation.mutate({ productId: product.id, data })}
                        onCancel={() => setSellingProductId(null)}
                      />
                    ) : (
                      <div className="flex items-center gap-3">
                        {isStaff && isActive(product) && (
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
                            {isActive(product) ? 'Editar' : 'Ver detalle'}
                          </Link>
                        )}
                      </div>
                    )}
                  </div>
                </div>
              </article>
            ))}
          </div>
        )}
        <Pagination pageData={productsQuery.data} onPageChange={setPage} />
      </div>
    </Layout>
  )
}
