import { apiFetch } from '../lib/apiClient'

export const appointmentsApi = {
  // Con date (YYYY-MM-DD) trae la agenda de ese día; sin ella, todos los turnos.
  list: (date) => apiFetch(date ? `/v1/appointments?date=${date}` : '/v1/appointments'),
  // El turno se crea colgando del beneficiario, igual que en el backend.
  listByApplicant: (applicantId) => apiFetch(`/v1/applicants/${applicantId}/appointments`),
  create: (applicantId, data) =>
    apiFetch(`/v1/applicants/${applicantId}/appointments`, { method: 'POST', body: data }),
  // Reprogramar mueve el mismo turno a otro día de atención; la hora la asigna el backend.
  reschedule: (appointmentId, appointmentDayId) =>
    apiFetch(`/v1/appointments/${appointmentId}/schedule`, {
      method: 'PUT',
      body: { appointmentDayId },
    }),
  cancel: (appointmentId, reason) =>
    apiFetch(`/v1/appointments/${appointmentId}/cancellation`, { method: 'PUT', body: { reason } }),
  // El administrativo revisó el comprobante y no encontró problemas: recién acá queda aceptado.
  approve: (appointmentId) =>
    apiFetch(`/v1/appointments/${appointmentId}/approval`, { method: 'PUT' }),
  registerAttendance: (appointmentId, attended) =>
    apiFetch(`/v1/appointments/${appointmentId}/attendance`, { method: 'PUT', body: { attended } }),
  // Autogestión: el applicantId sale del JWT en el backend, no se manda acá.
  listMine: () => apiFetch('/v1/me/appointments'),
  createMine: (data) => apiFetch('/v1/me/appointments', { method: 'POST', body: data }),
  // Confirma el turno (PENDING_PAYMENT → SCHEDULED) con el comprobante del bono contribución.
  uploadReceipt: (appointmentId, file) => {
    const body = new FormData()
    body.append('file', file)
    return apiFetch(`/v1/me/appointments/${appointmentId}/receipt`, { method: 'PUT', body })
  },
  // Staff: URL firmada para revisar el comprobante offline.
  getReceipt: (appointmentId) => apiFetch(`/v1/appointments/${appointmentId}/receipt`),
  // Días de atención (staff): de hoy en adelante, con franjas tomadas y libres.
  listDays: () => apiFetch('/v1/appointment-days'),
  createDay: (data) => apiFetch('/v1/appointment-days', { method: 'POST', body: data }),
  updateDay: (appointmentDayId, data) =>
    apiFetch(`/v1/appointment-days/${appointmentDayId}`, { method: 'PUT', body: data }),
  deleteDay: (appointmentDayId) =>
    apiFetch(`/v1/appointment-days/${appointmentDayId}`, { method: 'DELETE' }),
  // Autogestión: solo los días por venir que todavía tienen lugar.
  listBookableDays: () => apiFetch('/v1/me/appointment-days'),
}
