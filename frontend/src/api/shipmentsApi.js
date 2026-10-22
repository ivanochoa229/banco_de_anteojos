import { apiFetch } from '../lib/apiClient'

export const shipmentsApi = {
  // OUTBOUND: entre sucursales. INBOUND: donaciones que llegan por Vía Cargo.
  list: (direction) => apiFetch(`/v1/shipments?direction=${direction}`),
  // Trae el paquete, los marcos que lleva y el historial que empujó el carrier (RF-25).
  get: (shipmentId) => apiFetch(`/v1/shipments/${shipmentId}`),
  create: (data) => apiFetch('/v1/shipments', { method: 'POST', body: data }),
  // El número lo emite Vía Cargo; el backend lo registra en 17TRACK antes de guardarlo.
  dispatch: (shipmentId, trackingNumber) =>
    apiFetch(`/v1/shipments/${shipmentId}/dispatch`, { method: 'PUT', body: { trackingNumber } }),
  cancel: (shipmentId, reason) =>
    apiFetch(`/v1/shipments/${shipmentId}/cancellation`, { method: 'PUT', body: { reason } }),
  // Cierra una donación entrante con los marcos ya cargados bajo el donante.
  receive: (shipmentId, frameIds) =>
    apiFetch(`/v1/shipments/${shipmentId}/reception`, { method: 'PUT', body: { frameIds } }),
}
