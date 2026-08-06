import { apiFetch } from '../lib/apiClient'

export const applicantsApi = {
  list: () => apiFetch('/v1/applicants'),
  get: (applicantId) => apiFetch(`/v1/applicants/${applicantId}`),
  create: (data) => apiFetch('/v1/applicants', { method: 'POST', body: data }),
  update: (applicantId, data) => apiFetch(`/v1/applicants/${applicantId}`, { method: 'PUT', body: data }),
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
