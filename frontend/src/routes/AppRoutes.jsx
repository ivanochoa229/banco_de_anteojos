import { Navigate, Route, Routes } from 'react-router-dom'
import { useAuth } from '../context/useAuth'
import { ApplicantAnsesPage } from '../features/applicants/pages/ApplicantAnsesPage'
import { ApplicantCreatePage } from '../features/applicants/pages/ApplicantCreatePage'
import { ApplicantDashboardPage } from '../features/applicants/pages/ApplicantDashboardPage'
import { ApplicantEditPage } from '../features/applicants/pages/ApplicantEditPage'
import { ApplicantPrescriptionsPage } from '../features/applicants/pages/ApplicantPrescriptionsPage'
import { ApplicantsListPage } from '../features/applicants/pages/ApplicantsListPage'
import { AppointmentCreatePage } from '../features/appointments/pages/AppointmentCreatePage'
import { AppointmentsAgendaPage } from '../features/appointments/pages/AppointmentsAgendaPage'
import { MyAppointmentsPage } from '../features/appointments/pages/MyAppointmentsPage'
import { AssignmentCreatePage } from '../features/assignments/pages/AssignmentCreatePage'
import { AssignmentDetailPage } from '../features/assignments/pages/AssignmentDetailPage'
import { AssignmentsQueuePage } from '../features/assignments/pages/AssignmentsQueuePage'
import { MyStatusPage } from '../features/assignments/pages/MyStatusPage'
import { LoginPage } from '../features/auth/pages/LoginPage'
import { RegisterPage } from '../features/auth/pages/RegisterPage'
import { CatalogListPage } from '../features/catalog/pages/CatalogListPage'
import { ProductCreatePage } from '../features/catalog/pages/ProductCreatePage'
import { ProductEditPage } from '../features/catalog/pages/ProductEditPage'
import { DonorCreatePage } from '../features/donors/pages/DonorCreatePage'
import { DonorFramesPage } from '../features/donors/pages/DonorFramesPage'
import { DonorsListPage } from '../features/donors/pages/DonorsListPage'
import { FrameEditPage } from '../features/frames/pages/FrameEditPage'
import { FramesListPage } from '../features/frames/pages/FramesListPage'
import { AdminDashboardPage } from '../features/home/pages/AdminDashboardPage'
import { OperatorDashboardPage } from '../features/home/pages/OperatorDashboardPage'
import { IndicatorsPage } from '../features/indicators/pages/IndicatorsPage'
import { PublicLandingPage } from '../features/public/pages/PublicLandingPage'
import { ShipmentCreatePage } from '../features/shipments/pages/ShipmentCreatePage'
import { ShipmentDetailPage } from '../features/shipments/pages/ShipmentDetailPage'
import { ShipmentsListPage } from '../features/shipments/pages/ShipmentsListPage'
import { TryOnPage } from '../features/tryOn/pages/TryOnPage'
import { ProtectedRoute } from './ProtectedRoute'
import { RoleRoute } from './RoleRoute'

const STAFF_ROLES = ['ADMIN', 'OPERATOR']

export function AppRoutes() {
  const { isAuthenticated, role } = useAuth()

  const defaultPortalRoute =
    role === 'APPLICANT' ? '/solicitante' : role === 'OPERATOR' ? '/operador' : '/admin'

  return (
    <Routes>
      {/* 1. Landing Institucional Pública: ruta raíz abierta para toda la comunidad */}
      <Route path="/" element={<PublicLandingPage />} />

      {/* 2. Autenticación y Registro público exclusivo de solicitantes */}
      <Route
        path="/login"
        element={
          isAuthenticated ? <Navigate to={defaultPortalRoute} replace /> : <LoginPage />
        }
      />
      <Route
        path="/register"
        element={
          isAuthenticated ? <Navigate to="/solicitante" replace /> : <RegisterPage />
        }
      />

      {/* 3. Portales Privados: acceso exclusivamente autenticado */}
      <Route element={<ProtectedRoute />}>
        {/* ========================================================================= */}
        {/* PORTAL DEL SOLICITANTE (/solicitante/*) */}
        {/* ========================================================================= */}
        <Route element={<RoleRoute allowedRoles={['APPLICANT', 'OPERATOR', 'ADMIN']} />}>
          <Route path="/solicitante" element={<ApplicantDashboardPage />} />
          <Route path="/solicitante/mis-turnos" element={<MyAppointmentsPage />} />
          <Route path="/solicitante/mi-marco" element={<MyStatusPage />} />
          <Route path="/solicitante/probador" element={<TryOnPage />} />
          <Route path="/solicitante/catalogo" element={<CatalogListPage />} />
          {/* Rutas de autogestión directa */}
          <Route path="/my-appointments" element={<MyAppointmentsPage />} />
          <Route path="/my-status" element={<MyStatusPage />} />
        </Route>

        {/* ========================================================================= */}
        {/* PORTAL DEL OPERADOR (/operador/*) */}
        {/* ========================================================================= */}
        <Route element={<RoleRoute allowedRoles={STAFF_ROLES} />}>
          <Route path="/operador" element={<OperatorDashboardPage />} />
          <Route path="/operador/solicitantes" element={<ApplicantsListPage />} />
          <Route path="/operador/solicitantes/new" element={<ApplicantCreatePage />} />
          <Route
            path="/operador/solicitantes/:applicantId/edit"
            element={<ApplicantEditPage />}
          />
          <Route
            path="/operador/solicitantes/:applicantId/prescriptions"
            element={<ApplicantPrescriptionsPage />}
          />
          <Route
            path="/operador/solicitantes/:applicantId/anses-certificate"
            element={<ApplicantAnsesPage />}
          />
          <Route
            path="/operador/solicitantes/:applicantId/assignments/new"
            element={<AssignmentCreatePage />}
          />
          <Route
            path="/operador/solicitantes/:applicantId/appointments/new"
            element={<AppointmentCreatePage />}
          />
          <Route path="/operador/asignaciones" element={<AssignmentsQueuePage />} />
          <Route
            path="/operador/asignaciones/:assignmentId"
            element={<AssignmentDetailPage />}
          />
          <Route path="/operador/turnos" element={<AppointmentsAgendaPage />} />
          <Route path="/operador/donantes" element={<DonorsListPage />} />
          <Route path="/operador/donantes/new" element={<DonorCreatePage />} />
          <Route path="/operador/donantes/:donorId/frames" element={<DonorFramesPage />} />
          <Route path="/operador/marcos" element={<FramesListPage />} />
          <Route path="/operador/marcos/:frameId/edit" element={<FrameEditPage />} />
          <Route path="/operador/envios" element={<ShipmentsListPage />} />
          <Route path="/operador/envios/new" element={<ShipmentCreatePage />} />
          <Route path="/operador/envios/:shipmentId" element={<ShipmentDetailPage />} />
          <Route path="/operador/indicadores" element={<IndicatorsPage />} />
          <Route path="/operador/catalogo" element={<CatalogListPage />} />
          <Route path="/operador/probador" element={<TryOnPage />} />
        </Route>

        {/* ========================================================================= */}
        {/* PORTAL DEL ADMINISTRADOR (/admin/*) */}
        {/* ========================================================================= */}
        <Route element={<RoleRoute allowedRoles={['ADMIN']} />}>
          <Route path="/admin" element={<AdminDashboardPage />} />
          <Route path="/admin/solicitantes" element={<ApplicantsListPage />} />
          <Route path="/admin/solicitantes/new" element={<ApplicantCreatePage />} />
          <Route
            path="/admin/solicitantes/:applicantId/edit"
            element={<ApplicantEditPage />}
          />
          <Route
            path="/admin/solicitantes/:applicantId/prescriptions"
            element={<ApplicantPrescriptionsPage />}
          />
          <Route
            path="/admin/solicitantes/:applicantId/anses-certificate"
            element={<ApplicantAnsesPage />}
          />
          <Route
            path="/admin/solicitantes/:applicantId/assignments/new"
            element={<AssignmentCreatePage />}
          />
          <Route
            path="/admin/solicitantes/:applicantId/appointments/new"
            element={<AppointmentCreatePage />}
          />
          <Route path="/admin/asignaciones" element={<AssignmentsQueuePage />} />
          <Route path="/admin/asignaciones/:assignmentId" element={<AssignmentDetailPage />} />
          <Route path="/admin/turnos" element={<AppointmentsAgendaPage />} />
          <Route path="/admin/donantes" element={<DonorsListPage />} />
          <Route path="/admin/donantes/new" element={<DonorCreatePage />} />
          <Route path="/admin/donantes/:donorId/frames" element={<DonorFramesPage />} />
          <Route path="/admin/marcos" element={<FramesListPage />} />
          <Route path="/admin/marcos/:frameId/edit" element={<FrameEditPage />} />
          <Route path="/admin/envios" element={<ShipmentsListPage />} />
          <Route path="/admin/envios/new" element={<ShipmentCreatePage />} />
          <Route path="/admin/envios/:shipmentId" element={<ShipmentDetailPage />} />
          <Route path="/admin/indicadores" element={<IndicatorsPage />} />
          <Route path="/admin/catalogo" element={<CatalogListPage />} />
          <Route path="/admin/catalogo/new" element={<ProductCreatePage />} />
          <Route path="/admin/catalogo/:productId/edit" element={<ProductEditPage />} />
          <Route path="/admin/probador" element={<TryOnPage />} />
        </Route>

        {/* ========================================================================= */}
        {/* RUTAS OPERATIVAS COMUNES Y COMPATIBILIDAD CON VÍNCULOS EXISTENTES */}
        {/* ========================================================================= */}
        <Route element={<RoleRoute allowedRoles={STAFF_ROLES} />}>
          <Route path="/applicants" element={<ApplicantsListPage />} />
          <Route path="/applicants/new" element={<ApplicantCreatePage />} />
          <Route path="/applicants/:applicantId/edit" element={<ApplicantEditPage />} />
          <Route
            path="/applicants/:applicantId/prescriptions"
            element={<ApplicantPrescriptionsPage />}
          />
          <Route
            path="/applicants/:applicantId/anses-certificate"
            element={<ApplicantAnsesPage />}
          />
          <Route
            path="/applicants/:applicantId/assignments/new"
            element={<AssignmentCreatePage />}
          />
          <Route
            path="/applicants/:applicantId/appointments/new"
            element={<AppointmentCreatePage />}
          />
          <Route path="/assignments" element={<AssignmentsQueuePage />} />
          <Route path="/assignments/:assignmentId" element={<AssignmentDetailPage />} />
          <Route path="/appointments" element={<AppointmentsAgendaPage />} />
          <Route path="/donors" element={<DonorsListPage />} />
          <Route path="/donors/new" element={<DonorCreatePage />} />
          <Route path="/donors/:donorId/frames" element={<DonorFramesPage />} />
          <Route path="/frames" element={<FramesListPage />} />
          <Route path="/frames/:frameId/edit" element={<FrameEditPage />} />
          <Route path="/shipments" element={<ShipmentsListPage />} />
          <Route path="/shipments/new" element={<ShipmentCreatePage />} />
          <Route path="/shipments/:shipmentId" element={<ShipmentDetailPage />} />
          <Route path="/indicators" element={<IndicatorsPage />} />
          <Route path="/catalog" element={<CatalogListPage />} />
          <Route path="/try-on" element={<TryOnPage />} />
          <Route element={<RoleRoute allowedRoles={['ADMIN']} />}>
            <Route path="/catalog/new" element={<ProductCreatePage />} />
            <Route path="/catalog/:productId/edit" element={<ProductEditPage />} />
          </Route>
        </Route>
      </Route>

      {/* Redirección por defecto */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
