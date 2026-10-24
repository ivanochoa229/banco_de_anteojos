import { remainingValidityDays } from '../ansesValidity'

export function AnsesValidityNote({ issueDate }) {
  const remaining = remainingValidityDays(issueDate)
  if (remaining < 0) {
    const days = Math.abs(remaining)
    return (
      <span className="text-red-700">
        Vencida hace {days} {days === 1 ? 'día' : 'días'}
      </span>
    )
  }
  if (remaining === 0) return <span className="text-amber-700">Vence hoy</span>
  return (
    <span className={remaining <= 5 ? 'text-amber-700' : 'text-slate-600'}>
      Vence en {remaining} {remaining === 1 ? 'día' : 'días'}
    </span>
  )
}
