import { useEffect, useRef } from 'react'
import { drawTryOn } from '../framePlacement'

/**
 * El canvas se redibuja ante cualquier cambio: foto, marco o ajuste manual. No se ofrece
 * descargar el resultado a propósito: la foto del marco viene de una URL firmada de R2 y
 * dibujarla "mancha" el canvas, así que `toDataURL` fallaría sin configurar CORS en el bucket.
 */
export function TryOnCanvas({ photo, frameImage, placement, scale, verticalOffset }) {
  const canvasRef = useRef(null)

  useEffect(() => {
    if (!canvasRef.current || !photo) return
    drawTryOn(canvasRef.current, photo, frameImage, placement, { scale, verticalOffset })
  }, [photo, frameImage, placement, scale, verticalOffset])

  return (
    <canvas
      ref={canvasRef}
      className="w-full rounded-lg border border-slate-200 bg-slate-50"
      aria-label="Vista previa del marco sobre la foto"
    />
  )
}
