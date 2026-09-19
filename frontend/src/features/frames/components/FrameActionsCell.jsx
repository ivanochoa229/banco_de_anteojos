import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { framesApi } from '../../../api/framesApi'

/** Editar y dar de baja. La baja solo es posible desde el inventario disponible (lo valida el backend). */
export function FrameActionsCell({ frame }) {
  const queryClient = useQueryClient()
  const [confirming, setConfirming] = useState(false)

  const discardMutation = useMutation({
    mutationFn: () => framesApi.discard(frame.id),
    onSuccess: () => {
      setConfirming(false)
      queryClient.invalidateQueries({ queryKey: ['frames'] })
      queryClient.invalidateQueries({ queryKey: ['donors', frame.donorId, 'frames'] })
    },
  })

  if (frame.status !== 'AVAILABLE') {
    return <span className="text-slate-400">—</span>
  }

  if (confirming) {
    return (
      <div className="flex items-center gap-2">
        <span className="text-sm text-slate-600">¿Dar de baja?</span>
        <button
          type="button"
          onClick={() => discardMutation.mutate()}
          disabled={discardMutation.isPending}
          className="text-sm font-medium text-red-600 hover:underline disabled:text-red-300"
        >
          {discardMutation.isPending ? 'Dando de baja…' : 'Confirmar'}
        </button>
        <button
          type="button"
          onClick={() => setConfirming(false)}
          disabled={discardMutation.isPending}
          className="text-sm font-medium text-slate-500 hover:underline"
        >
          Cancelar
        </button>
      </div>
    )
  }

  return (
    <div className="flex items-center gap-3">
      <Link
        to={`/frames/${frame.id}/edit`}
        className="text-sm font-medium text-sky-700 hover:underline"
      >
        Editar
      </Link>
      <button
        type="button"
        onClick={() => setConfirming(true)}
        className="text-sm font-medium text-slate-500 hover:text-red-600 hover:underline"
      >
        Dar de baja
      </button>
      {discardMutation.error && (
        <span className="text-sm text-red-600">{discardMutation.error.message}</span>
      )}
    </div>
  )
}
