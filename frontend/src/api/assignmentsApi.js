import { apiFetch } from '../lib/apiClient'

export const assignmentsApi = {
  // Con live=true trae solo las que siguen en curso: la cola de trabajo del día.
  list: (live) => apiFetch(live ? '/v1/assignments?live=true' : '/v1/assignments'),
  get: (assignmentId) => apiFetch(`/v1/assignments/${assignmentId}`),
  // La asignación se crea colgando del beneficiario, igual que en el backend.
  listByApplicant: (applicantId) => apiFetch(`/v1/applicants/${applicantId}/assignments`),
  create: (applicantId, data) =>
    apiFetch(`/v1/applicants/${applicantId}/assignments`, { method: 'POST', body: data }),
  sendToOptician: (assignmentId) =>
    apiFetch(`/v1/assignments/${assignmentId}/optician-dispatch`, { method: 'PUT' }),
  returnFromOptician: (assignmentId) =>
    apiFetch(`/v1/assignments/${assignmentId}/optician-return`, { method: 'PUT' }),
  deliver: (assignmentId) => apiFetch(`/v1/assignments/${assignmentId}/delivery`, { method: 'PUT' }),
  cancel: (assignmentId, reason) =>
    apiFetch(`/v1/assignments/${assignmentId}/cancellation`, { method: 'PUT', body: { reason } }),
  // Autogestión: el applicantId sale del JWT en el backend, no se manda acá.
  listMine: () => apiFetch('/v1/me/assignments'),
}
