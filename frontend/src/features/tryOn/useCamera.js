import { useCallback, useEffect, useRef, useState } from 'react'

/**
 * Cámara de la notebook o del celular para sacar la foto sin salir de la app.
 *
 * `getUserMedia` solo existe en contexto seguro: anda en HTTPS y en localhost, pero **no** si se
 * entra al dev server por la IP de la red (http://192.168.x.x). En ese caso queda 'unsupported'
 * y la pantalla ofrece elegir un archivo, que en el celular igual abre la cámara.
 *
 * La cámara se usa solo para tomar una foto fija: la detección corre después, sobre esa imagen
 * estática, no cuadro a cuadro (RNF-04, compatibilidad con gama baja).
 */
export function useCamera() {
  const videoRef = useRef(null)
  const streamRef = useRef(null)
  // 'idle' | 'starting' | 'streaming' | 'unsupported' | 'denied' | 'error'
  const [status, setStatus] = useState('idle')
  const [error, setError] = useState(null)
  // 'user' es la cámara frontal (selfie); 'environment', la trasera del celular.
  const [facingMode, setFacingMode] = useState('user')

  const stop = useCallback(() => {
    streamRef.current?.getTracks().forEach((track) => track.stop())
    streamRef.current = null
    if (videoRef.current) videoRef.current.srcObject = null
    setStatus('idle')
  }, [])

  const start = useCallback(async (requestedFacingMode = facingMode) => {
    if (!navigator.mediaDevices?.getUserMedia) {
      setError(
        'El navegador no da acceso a la cámara desde esta dirección. Hace falta HTTPS (o localhost).',
      )
      setStatus('unsupported')
      return
    }

    setStatus('starting')
    setError(null)
    try {
      // facingMode no va como "exact": en una notebook con una sola cámara, pedir la trasera
      // no debe fallar, sino devolver la que haya.
      const stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: requestedFacingMode, width: { ideal: 1280 } },
        audio: false,
      })
      streamRef.current?.getTracks().forEach((track) => track.stop())
      streamRef.current = stream
      if (videoRef.current) {
        videoRef.current.srcObject = stream
        await videoRef.current.play()
      }
      setFacingMode(requestedFacingMode)
      setStatus('streaming')
    } catch (caught) {
      if (caught.name === 'NotAllowedError') {
        setError('No se permitió el acceso a la cámara. Habilitalo desde el candado de la barra.')
        setStatus('denied')
        return
      }
      if (caught.name === 'NotFoundError') {
        setError('No se encontró ninguna cámara en este dispositivo.')
        setStatus('error')
        return
      }
      setError(caught.message)
      setStatus('error')
    }
  }, [facingMode])

  const flip = useCallback(() => {
    start(facingMode === 'user' ? 'environment' : 'user')
  }, [facingMode, start])

  // Apagar la cámara al salir de la pantalla: si no, queda la luz prendida.
  useEffect(() => stop, [stop])

  /**
   * Congela el cuadro actual y lo devuelve como data URL. Con la cámara frontal se espeja, para
   * que la foto coincida con lo que la persona estaba viendo en la vista previa.
   */
  const capture = useCallback(() => {
    const video = videoRef.current
    if (!video?.videoWidth) return null

    const canvas = document.createElement('canvas')
    canvas.width = video.videoWidth
    canvas.height = video.videoHeight
    const context = canvas.getContext('2d')
    if (facingMode === 'user') {
      context.translate(canvas.width, 0)
      context.scale(-1, 1)
    }
    context.drawImage(video, 0, 0)
    return canvas.toDataURL('image/png')
  }, [facingMode])

  return { videoRef, status, error, facingMode, start, stop, flip, capture }
}
