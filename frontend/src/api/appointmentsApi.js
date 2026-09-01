import { apiFetch } from '../lib/apiClient'

export const appointmentsApi = {
  // Con date (YYYY-MM-DD) trae la agenda de ese día; sin ella, todos los turnos.
  list: (date) => apiFetch(date ? `/v1/appointments?date=${date}` : '/v1/appointments'),
  // El turno se crea colgando del beneficiario, igual que en el backend.
  listByApplicant: (applicantId) => apiFetch(`/v1/applicants/${applicantId}/appointments`),
  create: (applicantId, data) =>
    apiFetch(`/v1/applicants/${applicantId}/appointments`, { method: 'POST', body: data }),
  // Reprogramar pisa la fecha del mismo turno, no crea otro.
  reschedule: (appointmentId, scheduledAt) =>
    apiFetch(`/v1/appointments/${appointmentId}/schedule`, { method: 'PUT', body: { scheduledAt } }),
  cancel: (appointmentId, reason) =>
    apiFetch(`/v1/appointments/${appointmentId}/cancellation`, { method: 'PUT', body: { reason } }),
  registerAttendance: (appointmentId, attended) =>
    apiFetch(`/v1/appointments/${appointmentId}/attendance`, { method: 'PUT', body: { attended } }),
  // Autogestión: el applicantId sale del JWT en el backend, no se manda acá.
  listMine: () => apiFetch('/v1/me/appointments'),
  createMine: (data) => apiFetch('/v1/me/appointments', { method: 'POST', body: data }),
}
