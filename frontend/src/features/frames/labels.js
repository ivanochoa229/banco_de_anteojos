// Etiquetas en español de los enums del backend (FrameType, FrameMaterial, FrameStatus).

export const FRAME_TYPE_LABELS = {
  FULL_RIM: 'Aro completo',
  SEMI_RIMLESS: 'Medio aro',
  RIMLESS: 'Al aire',
}

export const FRAME_MATERIAL_LABELS = {
  ACETATE: 'Acetato',
  METAL: 'Metal',
  TITANIUM: 'Titanio',
  PLASTIC: 'Plástico',
  OTHER: 'Otro',
}

export const FRAME_STATUS_LABELS = {
  AVAILABLE: 'Disponible',
  ASSIGNED: 'Asignado',
  AT_OPTICIAN: 'En la óptica',
  READY: 'Listo para entregar',
  DELIVERED: 'Entregado',
  DISCARDED: 'Fuera de circulación',
}

// Color del badge de estado: verde lo que se puede usar, ámbar lo que está en proceso,
// gris lo que ya salió del circuito.
export const FRAME_STATUS_STYLES = {
  AVAILABLE: 'bg-green-50 text-green-700',
  ASSIGNED: 'bg-amber-50 text-amber-700',
  AT_OPTICIAN: 'bg-amber-50 text-amber-700',
  READY: 'bg-sky-50 text-sky-700',
  DELIVERED: 'bg-slate-100 text-slate-600',
  DISCARDED: 'bg-slate-100 text-slate-600',
}

function toOptions(labels) {
  return Object.entries(labels).map(([value, label]) => ({ value, label }))
}

export const FRAME_TYPE_OPTIONS = toOptions(FRAME_TYPE_LABELS)
export const FRAME_MATERIAL_OPTIONS = toOptions(FRAME_MATERIAL_LABELS)
export const FRAME_STATUS_OPTIONS = toOptions(FRAME_STATUS_LABELS)
