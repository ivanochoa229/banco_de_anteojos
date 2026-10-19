import { useState } from 'react'
import { Button } from '../../../components/Button'
import { Input } from '../../../components/Input'
import { Select } from '../../../components/Select'
import { appointmentDayLabel } from '../labels'

/**
 * Acciones de un turno. En PENDING_REVIEW el administrativo revisó el comprobante: aprueba (queda
 * SCHEDULED / aceptado) o cancela si tiene un problema. Ya SCHEDULED, registra la asistencia del
 * día (asistió/faltó), reprograma a otro día de atención (`days`, ya filtrado a los que tienen
 * lugar) o cancela. Reprogramar y cancelar despliegan su mini-formulario
 * en el lugar, igual que la cancelación de una asignación: confirmar de un click sin dato de
 * respaldo invita a errores.
 */
export function AppointmentActions({
  appointment,
  days,
  onReschedule,
  onCancel,
  onAttendance,
  onApprove,
  isPending,
}) {
  // 'idle' | 'rescheduling' | 'cancelling'
  const [mode, setMode] = useState('idle')
  const [newDayId, setNewDayId] = useState('')
  const [reason, setReason] = useState('')

  if (appointment.status !== 'SCHEDULED' && appointment.status !== 'PENDING_REVIEW') {
    return <span className="text-sm text-slate-400">—</span>
  }

  if (mode === 'rescheduling') {
    // Moverlo al mismo día no tiene sentido: el backend lo rechaza.
    const otherDays = days.filter((day) => day.id !== appointment.appointmentDayId)
    return (
      <form
        onSubmit={(event) => {
          event.preventDefault()
          if (newDayId) onReschedule(Number(newDayId))
        }}
        className="flex flex-wrap items-end gap-2"
      >
        <div className="w-80">
          {otherDays.length === 0 ? (
            <p className="text-sm text-slate-500">No hay otro día de atención con lugar.</p>
          ) : (
            <Select
              id={`new-day-${appointment.id}`}
              label="Nuevo día"
              required
              placeholder="Elegí un día"
              options={otherDays.map((day) => ({ value: day.id, label: appointmentDayLabel(day) }))}
              value={newDayId}
              onChange={(event) => setNewDayId(event.target.value)}
            />
          )}
        </div>
        <Button type="submit" disabled={isPending || !newDayId} className="px-3 py-2 text-sm">
          {isPending ? 'Reprogramando…' : 'Confirmar'}
        </Button>
        <button
          type="button"
          onClick={() => setMode('idle')}
          className="px-2 py-2 text-sm font-medium text-slate-500 hover:underline"
        >
          Volver
        </button>
      </form>
    )
  }

  if (mode === 'cancelling') {
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
            id={`reason-${appointment.id}`}
            label="Motivo (opcional)"
            maxLength={255}
            placeholder="Viaja esa semana"
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
          onClick={() => setMode('idle')}
          className="px-2 py-2 text-sm font-medium text-slate-500 hover:underline"
        >
          Volver
        </button>
      </form>
    )
  }

  if (appointment.status === 'PENDING_REVIEW') {
    return (
      <div className="flex flex-wrap items-center gap-3">
        <Button
          onClick={onApprove}
          disabled={isPending}
          className="bg-green-700 px-3 py-2 text-sm hover:bg-green-800"
        >
          Aprobar
        </Button>
        <button
          type="button"
          onClick={() => setMode('cancelling')}
          disabled={isPending}
          className="text-sm font-medium text-red-700 hover:underline disabled:opacity-60"
        >
          Cancelar
        </button>
      </div>
    )
  }

  return (
    <div className="flex flex-wrap items-center gap-3">
      <Button
        onClick={() => onAttendance(true)}
        disabled={isPending}
        className="bg-green-700 px-3 py-2 text-sm hover:bg-green-800"
      >
        Asistió
      </Button>
      <button
        type="button"
        onClick={() => onAttendance(false)}
        disabled={isPending}
        className="text-sm font-medium text-red-700 hover:underline disabled:opacity-60"
      >
        Faltó
      </button>
      <button
        type="button"
        onClick={() => setMode('rescheduling')}
        disabled={isPending}
        className="text-sm font-medium text-slate-500 hover:underline disabled:opacity-60"
      >
        Reprogramar
      </button>
      <button
        type="button"
        onClick={() => setMode('cancelling')}
        disabled={isPending}
        className="text-sm font-medium text-slate-500 hover:underline disabled:opacity-60"
      >
        Cancelar
      </button>
    </div>
  )
}
