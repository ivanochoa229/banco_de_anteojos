import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../context/useAuth'

// Guard por rol: envolver rutas que exigen roles específicos (p. ej. administración solo ADMIN).
export function RoleRoute({ allowedRoles }) {
  const { role } = useAuth()
  return allowedRoles.includes(role) ? <Outlet /> : <Navigate to="/" replace />
}
