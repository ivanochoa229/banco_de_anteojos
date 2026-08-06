import { useState } from 'react'
import { Button } from '../../../components/Button'
import { Input } from '../../../components/Input'
import { NEXT_MILESTONE, assignmentStage } from '../labels'

/**
 * Botonera del circuito: solo el hito que sigue, más cancelar. Mostrar los cuatro hitos
 * siempre invitaba a marcar uno fuera de orden y comerse el 409 del backend.
 *
 * El motivo de cancelación es opcional en la API, pero pedirlo explícito evita que se
 * cancele de un click sin dejar rastro de por qué.
 */
export function AssignmentActions({ assignment, onMilestone, onCancel, isPending }) {
  const [isCancelling, setIsCancelling] = useState(false)
  const [reason, setReason] = useState('')

  const next = NEXT_MILESTONE[assignmentStage(assignment)]
  if (!next) return <span className="text-sm text-slate-400">—</span>

  if (isCancelling) {
    return (
      <form
        onSubmit={(event) => {
          event.preventDefault()
          onCancel(reason.trim())
        }}
        className="flex flex-wrap items-end gap-2"
      >
        <div className="w-56">
          <Input
            id={`reason-${assignment.id}`}
            label="Motivo (opcional)"
            maxLength={255}
            placeholder="El marco no le calzó"
            value={reason}
            onChange={(event) => setReason(event.target.value)}
          />
        </div>
        <Button
          type="submit"
          disabled={isPending}
          className="bg-red-700 px-3 py-2 text-sm hover:bg-red-800"
        >
          {isPending ? 'Cancelando…' : 'Confirmar'}
        </Button>
        <button
          type="button"
          onClick={() => setIsCancelling(false)}
          className="px-2 py-2 text-sm font-medium text-slate-500 hover:underline"
        >
          Volver
        </button>
      </form>
    )
  }

  return (
    <div className="flex items-center gap-3">
      <Button
        onClick={() => onMilestone(next.action)}
        disabled={isPending}
        className="px-3 py-2 text-sm"
      >
        {next.label}
      </Button>
      <button
        type="button"
        onClick={() => setIsCancelling(true)}
        disabled={isPending}
        className="text-sm font-medium text-slate-500 hover:underline disabled:opacity-60"
      >
        Cancelar
      </button>
    </div>
  )
}
