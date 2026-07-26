import { apiFetch } from '../lib/apiClient'

export const applicantsApi = {
  list: () => apiFetch('/v1/applicants'),
  get: (applicantId) => apiFetch(`/v1/applicants/${applicantId}`),
  create: (data) => apiFetch('/v1/applicants', { method: 'POST', body: data }),
  update: (applicantId, data) => apiFetch(`/v1/applicants/${applicantId}`, { method: 'PUT', body: data }),
}
