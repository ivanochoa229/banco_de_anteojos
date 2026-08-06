// La asignación no tiene columna de estado: cada hito del circuito es un timestamp y el estado
// es el último que ocurrió. Esa regla vive solo acá para que las tres pantallas coincidan.

export function assignmentStage(assignment) {
  if (assignment.cancelledAt) return 'CANCELLED'
  if (assignment.deliveredAt) return 'DELIVERED'
  if (assignment.returnedAt) return 'READY'
  if (assignment.sentToOpticianAt) return 'AT_OPTICIAN'
  return 'ASSIGNED'
}

export const ASSIGNMENT_STAGE_LABELS = {
  ASSIGNED: 'Asignado',
  AT_OPTICIAN: 'En la óptica',
  READY: 'Listo para entregar',
  DELIVERED: 'Entregado',
  CANCELLED: 'Cancelado',
}

// Mismo criterio de color que el inventario: ámbar lo que está en proceso, verde lo que se
// puede hacer avanzar hoy, gris lo que ya salió del circuito.
export const ASSIGNMENT_STAGE_STYLES = {
  ASSIGNED: 'bg-amber-50 text-amber-700',
  AT_OPTICIAN: 'bg-amber-50 text-amber-700',
  READY: 'bg-green-50 text-green-700',
  DELIVERED: 'bg-slate-100 text-slate-600',
  CANCELLED: 'bg-slate-100 text-slate-600',
}

// El hito que el operador puede marcar según dónde está parada la asignación. Los estados
// terminales no aparecen: ya no se les puede hacer nada.
export const NEXT_MILESTONE = {
  ASSIGNED: { action: 'sendToOptician', label: 'Enviar a la óptica' },
  AT_OPTICIAN: { action: 'returnFromOptician', label: 'Registrar retorno' },
  READY: { action: 'deliver', label: 'Registrar entrega' },
}

// Línea de tiempo de la trazabilidad (RF-16), en el orden del circuito.
export const ASSIGNMENT_MILESTONES = [
  { field: 'assignedAt', label: 'Marco asignado' },
  { field: 'sentToOpticianAt', label: 'Enviado a la óptica con la receta' },
  { field: 'returnedAt', label: 'Volvió de la óptica con los cristales' },
  { field: 'deliveredAt', label: 'Entregado al beneficiario' },
]
