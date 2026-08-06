// La negativa de ANSES vale 30 días desde su emisión. El backend es el que decide (rechaza el
// upload vencido y arma el aviso de elegibilidad); acá el cálculo es solo para avisarle al
// operador cuántos días le quedan antes de que la persona vuelva a necesitar el trámite.
export const ANSES_VALIDITY_DAYS = 30

const MS_PER_DAY = 24 * 60 * 60 * 1000

/** issueDate viene como fecha pura (2026-08-01); se compara a medianoche local, sin husos. */
export function daysSinceIssue(issueDate) {
  const [year, month, day] = issueDate.split('-').map(Number)
  const issued = new Date(year, month - 1, day)
  const today = new Date()
  const midnight = new Date(today.getFullYear(), today.getMonth(), today.getDate())
  return Math.round((midnight - issued) / MS_PER_DAY)
}

export function remainingValidityDays(issueDate) {
  return ANSES_VALIDITY_DAYS - daysSinceIssue(issueDate)
}
