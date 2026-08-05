import { Link } from 'react-router-dom'
import { Layout } from '../../../components/Layout'

export function HomePage() {
  return (
    <Layout>
      <h2 className="text-xl font-semibold text-slate-900">Bienvenido</h2>
      <p className="mt-2 text-slate-600">
        Desde acá vas a poder gestionar solicitantes, donaciones, inventario y turnos.
        Los módulos se irán habilitando a medida que estén disponibles.
      </p>

      <div className="mt-8 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <Link
          to="/applicants"
          className="rounded-lg border border-slate-200 bg-white p-5 transition hover:border-sky-300 hover:shadow-sm"
        >
          <h3 className="font-semibold text-slate-900">Solicitantes</h3>
          <p className="mt-1 text-sm text-slate-500">
            Alta, edición y listado de beneficiarios con su DNI y datos de contacto.
          </p>
        </Link>

        <Link
          to="/donors"
          className="rounded-lg border border-slate-200 bg-white p-5 transition hover:border-sky-300 hover:shadow-sm"
        >
          <h3 className="font-semibold text-slate-900">Donantes</h3>
          <p className="mt-1 text-sm text-slate-500">
            Registro de quién donó y carga de los marcos que trajo cada donación.
          </p>
        </Link>

        <Link
          to="/frames"
          className="rounded-lg border border-slate-200 bg-white p-5 transition hover:border-sky-300 hover:shadow-sm"
        >
          <h3 className="font-semibold text-slate-900">Inventario</h3>
          <p className="mt-1 text-sm text-slate-500">
            Todos los marcos con su precinto, medidas y estado dentro del circuito.
          </p>
        </Link>
      </div>
    </Layout>
  )
}
