import { useCallback, useEffect, useRef, useState } from 'react'
import { FaceLandmarker, FilesetResolver } from '@mediapipe/tasks-vision'

// Ambos se sirven desde el propio dominio: el probador no le pega a ningún CDN ni servidor de
// procesamiento externo (RNF-04). El wasm lo copia vite.config.js desde node_modules.
const WASM_PATH = '/mediapipe/wasm'
const MODEL_PATH = '/models/face_landmarker.task'

/**
 * Carga el modelo de Face Mesh una sola vez y expone `detect(image)`.
 *
 * El modelo pesa ~3,6 MB y el wasm bastante más, así que la carga es lenta la primera vez y
 * puede fallar (memoria en un equipo de gama baja, wasm bloqueado). Por eso todo el ciclo de
 * vida vive acá y se expone como estado: la página muestra "probador no disponible" y el resto
 * de la app no se entera.
 */
export function useFaceLandmarker() {
  // 'loading' | 'ready' | 'unavailable'
  const [status, setStatus] = useState('loading')
  const [error, setError] = useState(null)
  const landmarkerRef = useRef(null)

  useEffect(() => {
    let cancelled = false

    async function load() {
      try {
        const fileset = await FilesetResolver.forVisionTasks(WASM_PATH)
        const landmarker = await FaceLandmarker.createFromOptions(fileset, {
          baseOptions: { modelAssetPath: MODEL_PATH },
          // IMAGE y no VIDEO: se trabaja sobre una foto estática, no en tiempo real.
          runningMode: 'IMAGE',
          numFaces: 1,
        })
        if (cancelled) {
          landmarker.close()
          return
        }
        landmarkerRef.current = landmarker
        setStatus('ready')
      } catch (caught) {
        if (cancelled) return
        setError(caught.message)
        setStatus('unavailable')
      }
    }

    load()
    return () => {
      cancelled = true
      landmarkerRef.current?.close()
      landmarkerRef.current = null
    }
  }, [])

  /** Devuelve los landmarks del primer rostro, o null si no se detectó ninguno. */
  const detect = useCallback((image) => {
    if (!landmarkerRef.current) return null
    const result = landmarkerRef.current.detect(image)
    return result.faceLandmarks?.[0] ?? null
  }, [])

  return { status, error, detect }
}
