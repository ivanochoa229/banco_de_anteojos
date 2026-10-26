import { useState } from 'react'
import { Link } from 'react-router-dom'
import { donorsApi } from '../../../api/donorsApi'
import { Alert } from '../../../components/Alert'
import { Button } from '../../../components/Button'
import { FramePicker } from '../../frames/components/FramePicker'

/**
 * Cierre de una donación entrante: el operador abre el paquete, carga cada marco bajo el donante
 * (con su precinto) y acá marca cuáles vinieron en esta donación. Solo se ofrecen los disponibles:
 * un marco asignado o entregado ya tuvo otra historia y no puede estar llegando ahora.
 *
 * Confirmar sin marcos también vale: el paquete pudo no traer nada aprovechable.
 */
export function ShipmentReceptionForm({ donorId, onSubmit, isPending, submitError }) {
  const [selectedFrames, setSelectedFrames] = useState([])

  return (
    <form
      onSubmit={(event) => {
        event.preventDefault()
        onSubmit(selectedFrames.map((frame) => frame.id))
      }}
      className="space-y-4"
    >
      <p className="text-sm text-slate-600">
        Al abrir el paquete, cargá cada marco con su precinto en{' '}
        <Link to={`/donors/${donorId}/frames`} className="font-medium text-sky-700 hover:underline">
          los marcos del donante
        </Link>{' '}
        y después marcá acá los que llegaron en esta donación.
      </p>

      <FramePicker
        id="receivedFrames"
        // Comparte el prefijo con la página de marcos del donante: cargar uno allá refresca acá.
        queryKey={['donors', String(donorId), 'frames', 'AVAILABLE']}
        fetchPage={({ page, size }) => donorsApi.listFrames(donorId, { status: 'AVAILABLE', page, size })}
        selected={selectedFrames}
        onChange={setSelectedFrames}
        multiple
        searchable={false}
        emptyMessage="El donante no tiene marcos disponibles cargados."
      />

      {submitError && <Alert>{submitError}</Alert>}

      <Button type="submit" disabled={isPending}>
        {isPending
          ? 'Registrando…'
          : selectedFrames.length === 0
            ? 'Confirmar recepción sin marcos'
            : `Confirmar recepción (${selectedFrames.length} marco${selectedFrames.length === 1 ? '' : 's'})`}
      </Button>
    </form>
  )
}
