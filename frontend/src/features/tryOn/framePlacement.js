// Índices del Face Mesh de MediaPipe. Son fijos: el modelo devuelve siempre los 468 puntos
// en el mismo orden. "Derecho" e "izquierdo" son del lado de la persona, no del que mira.
const RIGHT_EYE_OUTER_CORNER = 33
const LEFT_EYE_OUTER_CORNER = 263
const RIGHT_TEMPLE = 234
const LEFT_TEMPLE = 454

/**
 * De dónde a dónde va el marco sobre la foto.
 *
 * El ancho sale de la distancia entre las sienes y no de la de los ojos, porque un anteojo se
 * apoya en las sienes: es la medida que fija cuán ancho tiene que verse. La inclinación sale de
 * los ojos, que es la referencia estable para saber si la cabeza está ladeada.
 *
 * Los landmarks vienen normalizados (0 a 1) sobre las dimensiones de la imagen.
 */
export function computeFramePlacement(landmarks, imageWidth, imageHeight) {
  const at = (index) => ({
    x: landmarks[index].x * imageWidth,
    y: landmarks[index].y * imageHeight,
  })

  const rightEye = at(RIGHT_EYE_OUTER_CORNER)
  const leftEye = at(LEFT_EYE_OUTER_CORNER)
  const rightTemple = at(RIGHT_TEMPLE)
  const leftTemple = at(LEFT_TEMPLE)

  return {
    centerX: (rightEye.x + leftEye.x) / 2,
    centerY: (rightEye.y + leftEye.y) / 2,
    width: Math.hypot(leftTemple.x - rightTemple.x, leftTemple.y - rightTemple.y),
    angle: Math.atan2(leftEye.y - rightEye.y, leftEye.x - rightEye.x),
  }
}

/**
 * Dibuja la foto y encima el marco. `scale`, `verticalOffset` y `rotationOffsetDegrees` son los
 * ajustes manuales: el encuadre automático acierta el eje y la inclinación en la mayoría de los
 * casos, pero cuánto baja el anteojo sobre la nariz depende de cada cara y de cómo esté recortada
 * la foto del marco, y a veces el ángulo detectado necesita una corrección fina.
 */
export function drawTryOn(
  canvas,
  photo,
  frameImage,
  placement,
  { scale, verticalOffset, rotationOffsetDegrees = 0 },
) {
  const context = canvas.getContext('2d')
  canvas.width = photo.width
  canvas.height = photo.height
  context.clearRect(0, 0, canvas.width, canvas.height)
  context.drawImage(photo, 0, 0)

  if (!placement || !frameImage) return

  const width = placement.width * scale
  // Se respeta la proporción del PNG del marco: estirarlo lo deformaría.
  const height = width * (frameImage.height / frameImage.width)
  const offsetInPixels = placement.width * verticalOffset

  context.save()
  context.translate(placement.centerX, placement.centerY + offsetInPixels)
  context.rotate(placement.angle + (rotationOffsetDegrees * Math.PI) / 180)
  context.drawImage(frameImage, -width / 2, -height / 2, width, height)
  context.restore()
}
