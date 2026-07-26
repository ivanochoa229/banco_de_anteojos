import { Navigate, Route, Routes } from 'react-router-dom'
import { useAuth } from '../context/useAuth'
import { LoginPage } from '../features/auth/pages/LoginPage'
import { HomePage } from '../features/home/pages/HomePage'
import { ProtectedRoute } from './ProtectedRoute'

export function AppRoutes() {
  const { isAuthenticated } = useAuth()

  return (
    <Routes>
      <Route
        path="/login"
        element={isAuthenticated ? <Navigate to="/" replace /> : <LoginPage />}
      />
      <Route element={<ProtectedRoute />}>
        <Route path="/" element={<HomePage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
