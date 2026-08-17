// Etiquetas en español de ShipmentStatus. PENDING y CANCELLED son estados nuestros (el paquete
// todavía no salió), REGISTERED marca el despacho ya registrado en 17TRACK y el resto lo empuja
// el carrier por webhook.

export const SHIPMENT_STATUS_LABELS = {
  PENDING: 'Pendiente de despacho',
  CANCELLED: 'Cancelado',
  REGISTERED: 'Despachado',
  INFO_RECEIVED: 'Registrado por el correo',
  IN_TRANSIT: 'En tránsito',
  AVAILABLE_FOR_PICKUP: 'Disponible para retirar',
  OUT_FOR_DELIVERY: 'En reparto',
  DELIVERED: 'Entregado',
  DELIVERY_FAILURE: 'Entrega fallida',
  EXCEPTION: 'Con problema',
  EXPIRED: 'Sin novedades hace tiempo',
  NOT_FOUND: 'Sin información del correo',
}

// Mismo criterio de color que el inventario y las asignaciones: ámbar lo que espera una acción
// nuestra, sky lo que está viajando, verde lo que se puede ir a buscar hoy, gris lo que salió
// del circuito. El rojo queda para los estados donde el envío se complicó.
export const SHIPMENT_STATUS_STYLES = {
  PENDING: 'bg-amber-50 text-amber-700',
  CANCELLED: 'bg-slate-100 text-slate-600',
  REGISTERED: 'bg-sky-50 text-sky-700',
  INFO_RECEIVED: 'bg-sky-50 text-sky-700',
  IN_TRANSIT: 'bg-sky-50 text-sky-700',
  AVAILABLE_FOR_PICKUP: 'bg-green-50 text-green-700',
  OUT_FOR_DELIVERY: 'bg-sky-50 text-sky-700',
  DELIVERED: 'bg-slate-100 text-slate-600',
  DELIVERY_FAILURE: 'bg-red-50 text-red-700',
  EXCEPTION: 'bg-red-50 text-red-700',
  EXPIRED: 'bg-slate-100 text-slate-600',
  NOT_FOUND: 'bg-slate-100 text-slate-600',
}

/**
 * Despachar y cancelar son las dos únicas acciones nuestras, y las dos exigen que el paquete
 * siga pendiente: una vez despachado el estado es del carrier (el backend tira 409).
 */
export function isPending(shipment) {
  return shipment.status === 'PENDING'
}

// "En curso" es todo lo que todavía puede cambiar. Se filtra acá y no en la API porque
// GET /v1/shipments devuelve la lista completa, sin parámetro de filtrado.
const CLOSED_STATUSES = ['CANCELLED', 'DELIVERED', 'EXPIRED']

export function isLive(shipment) {
  return !CLOSED_STATUSES.includes(shipment.status)
}

// Hitos propios del paquete, en el orden del circuito. El detalle del viaje lo aporta el
// historial del carrier, que es una lista abierta.
export const SHIPMENT_MILESTONES = [
  { field: 'createdAt', label: 'Paquete armado' },
  { field: 'dispatchedAt', label: 'Despachado y registrado en el seguimiento' },
  { field: 'deliveredAt', label: 'Entregado en destino' },
]

// Los eventos guardan el estado crudo que mandó 17TRACK. Se traduce para mostrarlo, pero si el
// carrier informa uno que no conocemos se muestra tal cual vino: es lo que dijo él.
export const TRACKING_RAW_STATUS_LABELS = {
  InfoReceived: 'Registrado por el correo',
  InTransit: 'En tránsito',
  AvailableForPickup: 'Disponible para retirar',
  OutForDelivery: 'En reparto',
  Delivered: 'Entregado',
  DeliveryFailure: 'Entrega fallida',
  Exception: 'Con problema',
  Expired: 'Sin novedades hace tiempo',
  NotFound: 'Sin información',
}
