// A diferencia de las asignaciones, el turno sí tiene columna de estado en el backend:
// estos labels son la única traducción a UI para que todas las pantallas coincidan.

export const APPOINTMENT_STATUS_LABELS = {
  SCHEDULED: 'Agendado',
  COMPLETED: 'Asistió',
  MISSED: 'Ausente',
  CANCELLED: 'Cancelado',
}

// Celeste lo que está por venir, verde lo que terminó bien, rojo la falta (para que salte a la
// vista y se reprograme), gris lo que salió de la agenda.
export const APPOINTMENT_STATUS_STYLES = {
  SCHEDULED: 'bg-sky-50 text-sky-700',
  COMPLETED: 'bg-green-50 text-green-700',
  MISSED: 'bg-red-50 text-red-700',
  CANCELLED: 'bg-slate-100 text-slate-600',
}
