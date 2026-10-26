import { apiFetch, withQuery } from '../lib/apiClient'

export const applicantsApi = {
  // Paginado (page desde 0). q busca por apellido y nombre (sin importar tildes) o por DNI.
  list: ({ q, page, size } = {}) => apiFetch(withQuery('/v1/applicants', { q, page, size })),
  get: (applicantId) => apiFetch(`/v1/applicants/${applicantId}`),
  create: (data) => apiFetch('/v1/applicants', { method: 'POST', body: data }),
  update: (applicantId, data) => apiFetch(`/v1/applicants/${applicantId}`, { method: 'PUT', body: data }),
  // Autogestión: el backend resuelve el solicitante desde el JWT, por eso no lleva id.
  getMine: () => apiFetch('/v1/me'),
  updateMine: (data) => apiFetch('/v1/me', { method: 'PUT', body: data }),
  getMyAnsesCertificate: () => apiFetch('/v1/me/anses-certificate'),
  uploadMyAnsesCertificate: (file) => {
    const body = new FormData()
    body.append('file', file)
    return apiFetch('/v1/me/anses-certificate', { method: 'PUT', body })
  },
  listPrescriptions: (applicantId) => apiFetch(`/v1/applicants/${applicantId}/prescriptions`),
  createPrescription: (applicantId, data) =>
    apiFetch(`/v1/applicants/${applicantId}/prescriptions`, { method: 'POST', body: data }),
  uploadPrescriptionFile: (applicantId, prescriptionId, file) => {
    const body = new FormData()
    body.append('file', file)
    return apiFetch(`/v1/applicants/${applicantId}/prescriptions/${prescriptionId}/file`, {
      method: 'PUT',
      body,
    })
  },
  // Devuelve una URL firmada que vence: se pide en el momento de abrirla, no se guarda.
  getPrescriptionFile: (applicantId, prescriptionId) =>
    apiFetch(`/v1/applicants/${applicantId}/prescriptions/${prescriptionId}/file`),
  // La negativa de ANSES es un sub-recurso único: subir otra reemplaza a la anterior.
  getAnsesCertificate: (applicantId) => apiFetch(`/v1/applicants/${applicantId}/anses-certificate`),
  uploadAnsesCertificate: (applicantId, file) => {
    const body = new FormData()
    body.append('file', file)
    return apiFetch(`/v1/applicants/${applicantId}/anses-certificate`, { method: 'PUT', body })
  },
  getEligibility: (applicantId) => apiFetch(`/v1/applicants/${applicantId}/eligibility`),
}
