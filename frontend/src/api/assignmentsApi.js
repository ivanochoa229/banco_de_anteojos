import { apiFetch, withQuery } from '../lib/apiClient'

export const assignmentsApi = {
  // Paginado (page desde 0). Con live=true trae solo las que siguen en curso: la cola de trabajo
  // del día. Cada ítem es { assignment, applicantName, frameSealCode }.
  list: ({ live, page, size } = {}) =>
    apiFetch(withQuery('/v1/assignments', { live: live || undefined, page, size })),
  get: (assignmentId) => apiFetch(`/v1/assignments/${assignmentId}`),
  // La asignación se crea colgando del beneficiario, igual que en el backend.
  listByApplicant: (applicantId) => apiFetch(`/v1/applicants/${applicantId}/assignments`),
  create: (applicantId, data) =>
    apiFetch(`/v1/applicants/${applicantId}/assignments`, { method: 'POST', body: data }),
  sendToOptician: (assignmentId) =>
    apiFetch(`/v1/assignments/${assignmentId}/optician-dispatch`, { method: 'PUT' }),
  returnFromOptician: (assignmentId) =>
    apiFetch(`/v1/assignments/${assignmentId}/optician-return`, { method: 'PUT' }),
  // La foto del beneficiario con sus anteojos es opcional; photoConsent es su permiso para que
  // se le mande al donante.
  deliver: (assignmentId, { photo, photoConsent } = {}) => {
    if (!photo) return apiFetch(`/v1/assignments/${assignmentId}/delivery`, { method: 'PUT' })
    const body = new FormData()
    body.append('photo', photo)
    body.append('photoConsent', String(Boolean(photoConsent)))
    return apiFetch(`/v1/assignments/${assignmentId}/delivery`, { method: 'PUT', body })
  },
  // Devuelve una URL firmada que vence: se pide en el momento de abrirla, no se guarda.
  getDeliveryPhoto: (assignmentId) => apiFetch(`/v1/assignments/${assignmentId}/delivery-photo`),
  cancel: (assignmentId, reason) =>
    apiFetch(`/v1/assignments/${assignmentId}/cancellation`, { method: 'PUT', body: { reason } }),
  // Autogestión: el applicantId sale del JWT en el backend, no se manda acá.
  listMine: () => apiFetch('/v1/me/assignments'),
}
