// Datos que el registro deja como opcionales pero la fundación necesita para contactar y atender
// al beneficiario. El CUIL no está: no lo carga el beneficiario, sale de la negativa de ANSES.
export function missingProfileFields(applicant) {
  const missing = []
  if (!applicant.birthDate) missing.push('fecha de nacimiento')
  if (!applicant.phone) missing.push('teléfono')
  return missing
}
