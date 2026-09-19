// El recorte de fondo (segmentación de objeto saliente) solo detecta el contorno externo del
// marco: el hueco donde va cada cristal queda relleno como si fuera parte del marco, porque el
// modelo no sabe que ahí "no hay nada". Este paso lo complementa en cuatro etapas:
//
//   1. Se ubica el fondo "confirmado": los píxeles transparentes que tocan el borde de la
//      imagen, propagados por contigüidad.
//   2. Como el fondo detrás de un marco casi nunca es un solo color parejo (ej: el marco apoyado
//      sobre una mesa, con la pared de fondo — el cristal muestra pared arriba y mesa abajo), no
//      se compara contra un promedio global. Se calcula, para cada píxel, el color del fondo
//      confirmado más CERCANO geométricamente (BFS multi-fuente) y se compara contra ese: así el
//      hueco que muestra pared se compara con pared, y el que muestra mesa con mesa.
//   3. Cada píxel opaco cuyo color coincide con su fondo local de referencia se marca candidato.
//   4. Los candidatos se agrupan por regiones conectadas (con un cierre morfológico para juntar
//      los que quedaron salteados por brillos/reflejos del cristal) y se perforan región por
//      región completa, no píxel por píxel — así un hueco no queda "a medias".
//
// Sigue siendo best-effort: depende de que el fondo detrás del marco sea razonablemente
// reconocible desde el borde de la foto (no funciona bien si el marco tapa TODO el fondo, sin
// nada de fondo visible cerca del hueco).
//
// A propósito conservador: FrameImageEditor (pincel de borrado manual) es la red de seguridad
// para lo que queda sin perforar, pero no hay forma de "devolver" lo que este paso se come del
// marco. Ante la duda, mejor perforar de menos (se termina a mano) que de más.

const BACKGROUND_COLOR_DISTANCE_THRESHOLD = 18
const CLOSING_ITERATIONS = 1
const MIN_HOLE_PIXELS = 30
const OPAQUE_ALPHA_THRESHOLD = 128

async function drawToCanvas(source, width, height) {
  const bitmap = await createImageBitmap(source)
  const canvas = document.createElement('canvas')
  canvas.width = width
  canvas.height = height
  const ctx = canvas.getContext('2d')
  ctx.drawImage(bitmap, 0, 0, width, height)
  return ctx
}

/** Píxeles transparentes alcanzables desde el borde de la imagen, por contigüidad (4-vecinos). */
function floodFillBorderBackground(alpha, width, height) {
  const reached = new Uint8Array(width * height)
  const stack = []

  function seed(x, y) {
    const i = y * width + x
    if (!reached[i] && alpha[i] < OPAQUE_ALPHA_THRESHOLD) {
      reached[i] = 1
      stack.push(i)
    }
  }
  for (let x = 0; x < width; x++) {
    seed(x, 0)
    seed(x, height - 1)
  }
  for (let y = 0; y < height; y++) {
    seed(0, y)
    seed(width - 1, y)
  }

  while (stack.length > 0) {
    const i = stack.pop()
    const x = i % width
    const y = (i / width) | 0
    if (x > 0 && alpha[i - 1] < OPAQUE_ALPHA_THRESHOLD && !reached[i - 1]) {
      reached[i - 1] = 1
      stack.push(i - 1)
    }
    if (x < width - 1 && alpha[i + 1] < OPAQUE_ALPHA_THRESHOLD && !reached[i + 1]) {
      reached[i + 1] = 1
      stack.push(i + 1)
    }
    if (y > 0 && alpha[i - width] < OPAQUE_ALPHA_THRESHOLD && !reached[i - width]) {
      reached[i - width] = 1
      stack.push(i - width)
    }
    if (y < height - 1 && alpha[i + width] < OPAQUE_ALPHA_THRESHOLD && !reached[i + width]) {
      reached[i + width] = 1
      stack.push(i + width)
    }
  }
  return reached
}

/**
 * Para cada píxel, el color del fondo confirmado más cercano en distancia de grilla (BFS
 * multi-fuente, sin condición de color: solo cercanía geométrica). Es la referencia "local" de
 * fondo, en vez de un único promedio global que no representa bien un fondo con varias zonas
 * (pared + mesa, por ejemplo).
 */
function nearestBackgroundColor(confirmedBackground, originalData, width, height) {
  const pixelCount = width * height
  const nearest = new Uint8ClampedArray(pixelCount * 3)
  const visited = new Uint8Array(pixelCount)
  const queue = new Int32Array(pixelCount)
  let head = 0
  let tail = 0

  for (let i = 0; i < pixelCount; i++) {
    if (!confirmedBackground[i]) continue
    visited[i] = 1
    const offset = i * 4
    nearest[i * 3] = originalData[offset]
    nearest[i * 3 + 1] = originalData[offset + 1]
    nearest[i * 3 + 2] = originalData[offset + 2]
    queue[tail++] = i
  }
  if (tail === 0) return null

  while (head < tail) {
    const i = queue[head++]
    const x = i % width
    const y = (i / width) | 0
    const c = i * 3

    const neighbors = []
    if (x > 0) neighbors.push(i - 1)
    if (x < width - 1) neighbors.push(i + 1)
    if (y > 0) neighbors.push(i - width)
    if (y < height - 1) neighbors.push(i + width)

    for (const n of neighbors) {
      if (visited[n]) continue
      visited[n] = 1
      nearest[n * 3] = nearest[c]
      nearest[n * 3 + 1] = nearest[c + 1]
      nearest[n * 3 + 2] = nearest[c + 2]
      queue[tail++] = n
    }
  }
  return nearest
}

function dilate(mask, width, height) {
  const out = new Uint8Array(mask.length)
  for (let y = 0; y < height; y++) {
    for (let x = 0; x < width; x++) {
      const i = y * width + x
      if (mask[i]) {
        out[i] = 1
        continue
      }
      for (let dy = -1; dy <= 1 && !out[i]; dy++) {
        for (let dx = -1; dx <= 1 && !out[i]; dx++) {
          const nx = x + dx
          const ny = y + dy
          if (nx < 0 || ny < 0 || nx >= width || ny >= height) continue
          if (mask[ny * width + nx]) out[i] = 1
        }
      }
    }
  }
  return out
}

function erode(mask, width, height) {
  const out = new Uint8Array(mask.length)
  for (let y = 0; y < height; y++) {
    for (let x = 0; x < width; x++) {
      const i = y * width + x
      if (!mask[i]) continue
      let allNeighborsOn = true
      for (let dy = -1; dy <= 1 && allNeighborsOn; dy++) {
        for (let dx = -1; dx <= 1 && allNeighborsOn; dx++) {
          const nx = x + dx
          const ny = y + dy
          if (nx < 0 || ny < 0 || nx >= width || ny >= height || !mask[ny * width + nx]) {
            allNeighborsOn = false
          }
        }
      }
      out[i] = allNeighborsOn ? 1 : 0
    }
  }
  return out
}

/** Cierre morfológico: junta candidatos vecinos separados por ruido puntual (brillos, reflejos). */
function closeMask(mask, width, height, iterations) {
  let result = mask
  for (let i = 0; i < iterations; i++) result = dilate(result, width, height)
  for (let i = 0; i < iterations; i++) result = erode(result, width, height)
  return result
}

/** Regiones 4-conectadas de la máscara. Cada una indica si toca el borde de la imagen. */
function connectedComponents(mask, width, height) {
  const labeled = new Uint8Array(mask.length)
  const components = []

  for (let start = 0; start < mask.length; start++) {
    if (!mask[start] || labeled[start]) continue

    const pixels = [start]
    labeled[start] = 1
    let touchesBorder = false
    const stack = [start]

    while (stack.length > 0) {
      const i = stack.pop()
      const x = i % width
      const y = (i / width) | 0
      if (x === 0 || y === 0 || x === width - 1 || y === height - 1) touchesBorder = true

      const neighbors = []
      if (x > 0) neighbors.push(i - 1)
      if (x < width - 1) neighbors.push(i + 1)
      if (y > 0) neighbors.push(i - width)
      if (y < height - 1) neighbors.push(i + width)

      for (const n of neighbors) {
        if (mask[n] && !labeled[n]) {
          labeled[n] = 1
          pixels.push(n)
          stack.push(n)
        }
      }
    }
    components.push({ pixels, touchesBorder })
  }
  return components
}

/** originalFile: la foto tal cual la sacó el operador. cutOutBlob: salida del recorte de fondo. */
export async function punchLensHoles(originalFile, cutOutBlob) {
  const cutOutBitmap = await createImageBitmap(cutOutBlob)
  const { width, height } = cutOutBitmap

  const cutOutCanvas = document.createElement('canvas')
  cutOutCanvas.width = width
  cutOutCanvas.height = height
  const cutOutCtx = cutOutCanvas.getContext('2d')
  cutOutCtx.drawImage(cutOutBitmap, 0, 0)

  const originalCtx = await drawToCanvas(originalFile, width, height)
  const originalData = originalCtx.getImageData(0, 0, width, height).data
  const cutOutImageData = cutOutCtx.getImageData(0, 0, width, height)
  const cutOutData = cutOutImageData.data

  const pixelCount = width * height
  const alpha = new Uint8ClampedArray(pixelCount)
  for (let i = 0; i < pixelCount; i++) alpha[i] = cutOutData[i * 4 + 3]

  const confirmedBackground = floodFillBorderBackground(alpha, width, height)
  const localBackground = nearestBackgroundColor(confirmedBackground, originalData, width, height)
  if (!localBackground) return cutOutBlob // no se encontró fondo confirmado: no hay nada para perforar

  const candidates = new Uint8Array(pixelCount)
  for (let i = 0; i < pixelCount; i++) {
    if (confirmedBackground[i] || alpha[i] < OPAQUE_ALPHA_THRESHOLD) continue
    const offset = i * 4
    const refOffset = i * 3
    const dr = originalData[offset] - localBackground[refOffset]
    const dg = originalData[offset + 1] - localBackground[refOffset + 1]
    const db = originalData[offset + 2] - localBackground[refOffset + 2]
    if (Math.sqrt(dr * dr + dg * dg + db * db) < BACKGROUND_COLOR_DISTANCE_THRESHOLD) {
      candidates[i] = 1
    }
  }

  const closedCandidates = closeMask(candidates, width, height, CLOSING_ITERATIONS)
  const components = connectedComponents(closedCandidates, width, height)

  for (const { pixels, touchesBorder } of components) {
    if (touchesBorder || pixels.length < MIN_HOLE_PIXELS) continue
    for (const i of pixels) cutOutData[i * 4 + 3] = 0
  }

  cutOutCtx.putImageData(cutOutImageData, 0, 0)
  return new Promise((resolve, reject) => {
    cutOutCanvas.toBlob((blob) => {
      if (blob) resolve(blob)
      else reject(new Error('No se pudo perforar los huecos de los cristales'))
    }, 'image/png')
  })
}
