import { useEffect, useRef, useState } from 'react'
import { Button } from '../../../components/Button'
import { cropToContent } from '../lib/cropToContent'

const BRUSH_SIZES = [
  { value: 20, label: 'Chico' },
  { value: 45, label: 'Mediano' },
  { value: 90, label: 'Grande' },
]

const TOOLS = [
  { value: 'brush', label: 'Pincel' },
  { value: 'wand', label: 'Varita mágica' },
]

// Tolerancia de color de la varita: un click borra toda la región conectada de tono parecido,
// no solo el píxel exacto (como pintar con balde en un editor de imágenes).
const WAND_COLOR_TOLERANCE = 30

// Fondo a cuadros clásico para que se note la transparencia real detrás del PNG.
const CHECKERBOARD_STYLE = {
  backgroundImage:
    'linear-gradient(45deg, #cbd5e1 25%, transparent 25%), linear-gradient(-45deg, #cbd5e1 25%, transparent 25%), linear-gradient(45deg, transparent 75%, #cbd5e1 75%), linear-gradient(-45deg, transparent 75%, #cbd5e1 75%)',
  backgroundSize: '20px 20px',
  backgroundPosition: '0 0, 0 10px, 10px -10px, -10px 0px',
}

/**
 * Pincel de borrado sobre el PNG ya recortado, para cuando el algoritmo automático deja
 * relleno sin perforar (marcos oscuros, sombras del propio cristal — casos que el recorte
 * automático no resuelve del todo). El operador termina de limpiarlo a mano antes de subir.
 */
export function FrameImageEditor({ imageFile, onConfirm, onCancel }) {
  const canvasRef = useRef(null)
  const cursorCanvasRef = useRef(null)
  const historyRef = useRef([])
  const isDrawingRef = useRef(false)
  const [brushSize, setBrushSize] = useState(BRUSH_SIZES[1].value)
  const [tool, setTool] = useState('brush')
  const [canUndo, setCanUndo] = useState(false)
  const [dimensions, setDimensions] = useState(null)

  useEffect(() => {
    let cancelled = false
    async function load() {
      const bitmap = await createImageBitmap(imageFile)
      if (cancelled) return
      setDimensions({ width: bitmap.width, height: bitmap.height })
      // Se dibuja recién en el siguiente render, cuando el canvas ya tiene el tamaño puesto.
      requestAnimationFrame(() => {
        const ctx = canvasRef.current?.getContext('2d')
        if (ctx) ctx.drawImage(bitmap, 0, 0)
      })
    }
    load()
    return () => {
      cancelled = true
    }
  }, [imageFile])

  function canvasPoint(event) {
    const canvas = canvasRef.current
    const rect = canvas.getBoundingClientRect()
    const scaleX = canvas.width / rect.width
    const scaleY = canvas.height / rect.height
    return {
      x: (event.clientX - rect.left) * scaleX,
      y: (event.clientY - rect.top) * scaleY,
    }
  }

  function eraseAt(x, y) {
    const ctx = canvasRef.current.getContext('2d')
    ctx.globalCompositeOperation = 'destination-out'
    ctx.beginPath()
    ctx.arc(x, y, brushSize / 2, 0, Math.PI * 2)
    ctx.fill()
  }

/** Click y borra toda la región 4-conectada de color parecido al píxel clickeado. */
  function magicWandErase(x, y) {
    const canvas = canvasRef.current
    const ctx = canvas.getContext('2d')
    const width = canvas.width
    const height = canvas.height
    const imageData = ctx.getImageData(0, 0, width, height)
    const data = imageData.data

    const startX = Math.floor(Math.min(Math.max(x, 0), width - 1))
    const startY = Math.floor(Math.min(Math.max(y, 0), height - 1))
    const start = startY * width + startX
    if (data[start * 4 + 3] === 0) return // ya es transparente, nada que hacer

    const seedR = data[start * 4]
    const seedG = data[start * 4 + 1]
    const seedB = data[start * 4 + 2]

    const visited = new Uint8Array(width * height)
    visited[start] = 1
    const stack = [start]

    while (stack.length > 0) {
      const i = stack.pop()
      const offset = i * 4
      if (data[offset + 3] === 0) continue

      const dr = data[offset] - seedR
      const dg = data[offset + 1] - seedG
      const db = data[offset + 2] - seedB
      if (Math.sqrt(dr * dr + dg * dg + db * db) > WAND_COLOR_TOLERANCE) continue

      data[offset + 3] = 0

      const px = i % width
      const py = (i / width) | 0
      if (px > 0 && !visited[i - 1]) {
        visited[i - 1] = 1
        stack.push(i - 1)
      }
      if (px < width - 1 && !visited[i + 1]) {
        visited[i + 1] = 1
        stack.push(i + 1)
      }
      if (py > 0 && !visited[i - width]) {
        visited[i - width] = 1
        stack.push(i - width)
      }
      if (py < height - 1 && !visited[i + width]) {
        visited[i + width] = 1
        stack.push(i + width)
      }
    }

    ctx.putImageData(imageData, 0, 0)
  }

  function drawCursor(x, y) {
    const cursorCanvas = cursorCanvasRef.current
    const ctx = cursorCanvas.getContext('2d')
    ctx.clearRect(0, 0, cursorCanvas.width, cursorCanvas.height)
    if (tool !== 'brush') return
    ctx.beginPath()
    ctx.arc(x, y, brushSize / 2, 0, Math.PI * 2)
    ctx.strokeStyle = 'rgba(15, 23, 42, 0.7)'
    ctx.lineWidth = Math.max(1, brushSize * 0.03)
    ctx.stroke()
  }

  function clearCursor() {
    const cursorCanvas = cursorCanvasRef.current
    cursorCanvas.getContext('2d').clearRect(0, 0, cursorCanvas.width, cursorCanvas.height)
  }

  function handlePointerDown(event) {
    const canvas = canvasRef.current
    historyRef.current.push(canvas.getContext('2d').getImageData(0, 0, canvas.width, canvas.height))
    setCanUndo(true)
    const { x, y } = canvasPoint(event)

    if (tool === 'wand') {
      magicWandErase(x, y)
      return
    }
    isDrawingRef.current = true
    eraseAt(x, y)
  }

  function handlePointerMove(event) {
    const { x, y } = canvasPoint(event)
    drawCursor(x, y)
    if (tool === 'brush' && isDrawingRef.current) eraseAt(x, y)
  }

  function handlePointerUp() {
    isDrawingRef.current = false
  }

  function handleUndo() {
    const last = historyRef.current.pop()
    if (!last) return
    canvasRef.current.getContext('2d').putImageData(last, 0, 0)
    setCanUndo(historyRef.current.length > 0)
  }

  // El probador virtual asume que el PNG ES el marco: recortar al contenido real evita que le
  // quede margen transparente alrededor, que lo haría ver más chico y descentrado sobre la cara.
  function handleConfirm() {
    cropToContent(canvasRef.current).toBlob((blob) => {
      if (blob) onConfirm(blob)
    }, 'image/png')
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/70 p-4">
      <div className="flex max-h-full max-w-3xl flex-col gap-4 rounded-lg bg-white p-6">
        <div>
          <h3 className="text-lg font-semibold text-slate-900">Retocar la foto del marco</h3>
          <p className="mt-1 text-sm text-slate-500">
            Si quedó relleno donde debería verse a través del cristal, borralo antes de subir la
            foto.
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-3">
          {TOOLS.map(({ value, label }) => (
            <button
              key={value}
              type="button"
              onClick={() => setTool(value)}
              className={`rounded-full px-3 py-1 text-sm font-medium transition ${
                tool === value
                  ? 'bg-slate-800 text-white'
                  : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
              }`}
            >
              {label}
            </button>
          ))}

          {tool === 'brush' && (
            <>
              <span className="text-sm font-medium text-slate-700">Tamaño:</span>
              {BRUSH_SIZES.map(({ value, label }) => (
                <button
                  key={value}
                  type="button"
                  onClick={() => setBrushSize(value)}
                  className={`rounded-full px-3 py-1 text-sm font-medium transition ${
                    brushSize === value
                      ? 'bg-sky-700 text-white'
                      : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                  }`}
                >
                  {label}
                </button>
              ))}
            </>
          )}

          <button
            type="button"
            onClick={handleUndo}
            disabled={!canUndo}
            className="ml-auto text-sm font-medium text-slate-500 hover:text-slate-700 hover:underline disabled:text-slate-300 disabled:no-underline"
          >
            Deshacer
          </button>
        </div>
        <p className="-mt-2 text-sm text-slate-500">
          {tool === 'brush'
            ? 'Pintá sobre el relleno sobrante para borrarlo.'
            : 'Hacé click sobre el relleno: borra toda la zona conectada de color parecido de una.'}
        </p>

        <div
          className="relative mx-auto overflow-auto rounded-lg border border-slate-200"
          style={CHECKERBOARD_STYLE}
        >
          {dimensions && (
            <>
              <canvas
                ref={canvasRef}
                width={dimensions.width}
                height={dimensions.height}
                className="block max-h-[60vh] max-w-full touch-none"
                onPointerDown={handlePointerDown}
                onPointerMove={handlePointerMove}
                onPointerUp={handlePointerUp}
                onPointerLeave={() => {
                  handlePointerUp()
                  clearCursor()
                }}
              />
              <canvas
                ref={cursorCanvasRef}
                width={dimensions.width}
                height={dimensions.height}
                className="pointer-events-none absolute inset-0 block max-h-[60vh] max-w-full"
              />
            </>
          )}
        </div>

        <div className="flex justify-end gap-3">
          <button
            type="button"
            onClick={onCancel}
            className="rounded-lg px-4 py-2.5 font-medium text-slate-600 hover:bg-slate-100"
          >
            Cancelar
          </button>
          <Button type="button" onClick={handleConfirm}>
            Usar esta foto
          </Button>
        </div>
      </div>
    </div>
  )
}
