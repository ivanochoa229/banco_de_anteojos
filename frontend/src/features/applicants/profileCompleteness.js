// Datos que el registro deja como opcionales pero la fundación necesita para contactar y atender
// al beneficiario. El CUIL no se tipea: queda cargado al validarse la negativa de ANSES.
export function missingProfileFields(applicant) {
  const missing = []
  if (!applicant.birthDate) missing.push('fecha de nacimiento')
  if (!applicant.phone) missing.push('teléfono')
  if (!applicant.cuil) missing.push('certificación negativa de ANSES')
  return missing
}

// "a", "a y b", "a, b y c"
export function joinFields(fields) {
  if (fields.length <= 1) return fields.join('')
  return `${fields.slice(0, -1).join(', ')} y ${fields.at(-1)}`
}
