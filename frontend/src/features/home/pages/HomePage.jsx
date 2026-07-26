import { useAuth } from '../../../context/useAuth'
import { Button } from '../../../components/Button'

const ROLE_LABELS = { ADMIN: 'Administrador', OPERATOR: 'Operador' }

export function HomePage() {
  const { role, logout } = useAuth()

  return (
    <div className="min-h-screen bg-slate-100">
      <header className="flex items-center justify-between border-b border-slate-200 bg-white px-6 py-4">
        <div>
          <h1 className="text-lg font-bold text-slate-900">Banco de Anteojos</h1>
          <p className="text-xs text-slate-500">Fundación Hacer Futuro</p>
        </div>
        <div className="flex items-center gap-4">
          {role && (
            <span className="rounded-full bg-sky-50 px-3 py-1 text-xs font-medium text-sky-700">
              {ROLE_LABELS[role] ?? role}
            </span>
          )}
          <Button onClick={logout} className="bg-slate-600 px-3 py-1.5 text-sm hover:bg-slate-700">
            Cerrar sesión
          </Button>
        </div>
      </header>

      <main className="mx-auto max-w-4xl px-6 py-12">
        <h2 className="text-xl font-semibold text-slate-900">Bienvenido</h2>
        <p className="mt-2 text-slate-600">
          Desde acá vas a poder gestionar solicitantes, donaciones, inventario y turnos.
          Los módulos se irán habilitando a medida que estén disponibles.
        </p>
      </main>
    </div>
  )
}
