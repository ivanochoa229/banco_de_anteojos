import { Navigate, Route, Routes } from 'react-router-dom'
import { useAuth } from '../context/useAuth'
import { ApplicantCreatePage } from '../features/applicants/pages/ApplicantCreatePage'
import { ApplicantEditPage } from '../features/applicants/pages/ApplicantEditPage'
import { ApplicantPrescriptionsPage } from '../features/applicants/pages/ApplicantPrescriptionsPage'
import { ApplicantsListPage } from '../features/applicants/pages/ApplicantsListPage'
import { AssignmentCreatePage } from '../features/assignments/pages/AssignmentCreatePage'
import { AssignmentDetailPage } from '../features/assignments/pages/AssignmentDetailPage'
import { AssignmentsQueuePage } from '../features/assignments/pages/AssignmentsQueuePage'
import { LoginPage } from '../features/auth/pages/LoginPage'
import { DonorCreatePage } from '../features/donors/pages/DonorCreatePage'
import { DonorFramesPage } from '../features/donors/pages/DonorFramesPage'
import { DonorsListPage } from '../features/donors/pages/DonorsListPage'
import { FramesListPage } from '../features/frames/pages/FramesListPage'
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
        <Route
          path="/applicants/:applicantId/prescriptions"
          element={<ApplicantPrescriptionsPage />}
        />
        <Route
          path="/applicants/:applicantId/assignments/new"
          element={<AssignmentCreatePage />}
        />
        <Route path="/assignments" element={<AssignmentsQueuePage />} />
        <Route path="/assignments/:assignmentId" element={<AssignmentDetailPage />} />
        <Route path="/donors" element={<DonorsListPage />} />
        <Route path="/donors/new" element={<DonorCreatePage />} />
        <Route path="/donors/:donorId/frames" element={<DonorFramesPage />} />
        <Route path="/frames" element={<FramesListPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
