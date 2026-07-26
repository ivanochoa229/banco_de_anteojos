import { Navigate, Route, Routes } from 'react-router-dom'
import { useAuth } from '../context/useAuth'
import { ApplicantCreatePage } from '../features/applicants/pages/ApplicantCreatePage'
import { ApplicantEditPage } from '../features/applicants/pages/ApplicantEditPage'
import { ApplicantsListPage } from '../features/applicants/pages/ApplicantsListPage'
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
        <Route path="/applicants" element={<ApplicantsListPage />} />
        <Route path="/applicants/new" element={<ApplicantCreatePage />} />
        <Route path="/applicants/:applicantId/edit" element={<ApplicantEditPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
