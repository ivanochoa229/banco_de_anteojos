// El probador virtual (framePlacement.js) calcula el tamaño y el centro del marco asumiendo
// que la foto ES el marco: usa naturalWidth/naturalHeight tal cual y centra la imagen entera
// sobre el rostro. Si el PNG le queda con margen transparente alrededor (el canvas del editor
// conserva el tamaño de la foto original), esa cuenta queda mal — el marco se ve más chico de
// lo que corresponde y descentrado. Por eso se recorta al contenido real antes de subir.

const PADDING_PX = 4

/** Rectángulo que encierra los píxeles no transparentes, o null si la imagen quedó vacía. */
function contentBounds(imageData, width, height) {
  const { data } = imageData
  let minX = width;
  let minY = height;
  let maxX = -1;
  let maxY = -1

  for (let y = 0; y < height; y++) {
    for (let x = 0; x < width; x++) {
      if (data[(y * width + x) * 4 + 3] === 0) continue
      if (x < minX) minX = x
      if (x > maxX) maxX = x
      if (y < minY) minY = y
      if (y > maxY) maxY = y
    }
  }
  if (maxX < minX || maxY < minY) return null
  return { minX, minY, maxX, maxY }
}

/** Recorta un canvas al rectángulo de contenido no transparente, con un margen chico. */
export function cropToContent(sourceCanvas) {
  const { width, height } = sourceCanvas
  const ctx = sourceCanvas.getContext('2d')
  const bounds = contentBounds(ctx.getImageData(0, 0, width, height), width, height)
  if (!bounds) return sourceCanvas // sin contenido: no hay nada para recortar

  const x0 = Math.max(0, bounds.minX - PADDING_PX)
  const y0 = Math.max(0, bounds.minY - PADDING_PX)
  const x1 = Math.min(width, bounds.maxX + PADDING_PX + 1)
  const y1 = Math.min(height, bounds.maxY + PADDING_PX + 1)

  const cropCanvas = document.createElement('canvas')
  cropCanvas.width = x1 - x0
  cropCanvas.height = y1 - y0
  cropCanvas
    .getContext('2d')
    .drawImage(sourceCanvas, x0, y0, cropCanvas.width, cropCanvas.height, 0, 0, cropCanvas.width, cropCanvas.height)
  return cropCanvas
}
