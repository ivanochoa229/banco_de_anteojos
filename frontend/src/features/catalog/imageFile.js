// Los tipos son los que acepta el backend; se chequean antes de subir para no esperar al servidor
// por un archivo que va a rechazar. El tope de 5 MB es solo del front: una foto de catálogo no
// necesita más.
const MAX_FILE_BYTES = 5 * 1024 * 1024

export const ACCEPTED_IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/webp']

export function validateImageFile(file) {
  if (!ACCEPTED_IMAGE_TYPES.includes(file.type)) return 'La imagen debe ser JPG, PNG o WEBP'
  if (file.size > MAX_FILE_BYTES) return 'La imagen supera los 5 MB'
  return null
}
