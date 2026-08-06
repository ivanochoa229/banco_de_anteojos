// Formato de la graduación, compartido entre el historial de recetas y el alta de asignación.

// Las dioptrías se leen siempre con signo y dos decimales (+1.00 / -0.75).
export function formatDiopter(value) {
  if (value === null || value === undefined) return '—'
  const number = Number(value)
  return `${number > 0 ? '+' : ''}${number.toFixed(2)}`
}

export function formatEye(sphere, cylinder, axis) {
  if (sphere === null && cylinder === null && axis === null) return '—'
  const parts = [`Esf ${formatDiopter(sphere)}`, `Cil ${formatDiopter(cylinder)}`]
  if (axis !== null && axis !== undefined) parts.push(`Eje ${axis}°`)
  return parts.join(' · ')
}
