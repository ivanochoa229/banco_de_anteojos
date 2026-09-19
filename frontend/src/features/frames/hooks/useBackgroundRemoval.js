import { useCallback, useState } from 'react'
import { removeBackground } from '@imgly/background-removal'
import { punchLensHoles } from '../lib/punchLensHoles'

/**
 * Recorta el fondo de la foto en el navegador (WASM, sin servidor de terceros) para que el
 * marco quede con canal alfa, tal como lo exige el backend (RF-18: se superpone sobre la cara
 * en el probador virtual). Falla de forma aislada: un error acá no debe frenar el resto de la
 * pantalla, solo impedir esa subida puntual.
 */
export function useBackgroundRemoval() {
  const [isProcessing, setIsProcessing] = useState(false)

  const process = useCallback(async (file) => {
    setIsProcessing(true)
    try {
      const cutOut = await removeBackground(file, { output: { format: 'image/png' } })

      // El recorte de fondo no perfora el hueco de los cristales (ver punchLensHoles). Si este
      // paso falla, mejor subir la foto sin perforar que bloquear la carga entera.
      let finalBlob = cutOut
      try {
        finalBlob = await punchLensHoles(file, cutOut)
      } catch {
        // best-effort: se sube el recorte tal cual salió del modelo.
      }

      return new File([finalBlob], file.name.replace(/\.[^.]+$/, '') + '.png', { type: 'image/png' })
    } catch {
      throw new Error('No se pudo recortar el fondo de la foto. Probá con otra imagen.')
    } finally {
      setIsProcessing(false)
    }
  }, [])

  return { isProcessing, process }
}
