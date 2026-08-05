// Etiquetas en español de los enums del backend (DonorType).
export const DONOR_TYPE_LABELS = {
  INDIVIDUAL: 'Particular',
  ORGANIZATION: 'Organización',
}

export const DONOR_TYPE_OPTIONS = Object.entries(DONOR_TYPE_LABELS).map(([value, label]) => ({
  value,
  label,
}))
