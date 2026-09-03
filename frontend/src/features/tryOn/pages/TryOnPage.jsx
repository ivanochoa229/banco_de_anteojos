import { useRef, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { framesApi } from '../../../api/framesApi'
import { Alert } from '../../../components/Alert'
import { Layout } from '../../../components/Layout'
import { FRAME_MATERIAL_LABELS, FRAME_TYPE_LABELS } from '../../frames/labels'
import { CameraCapture } from '../components/CameraCapture'
import { TryOnCanvas } from '../components/TryOnCanvas'
import { computeFramePlacement } from '../framePlacement'
import { loadImage } from '../loadImage'
import { useCamera } from '../useCamera'
import { useFaceLandmarker } from '../useFaceLandmarker'

const MAX_PHOTO_BYTES = 10 * 1024 * 1024

export function TryOnPage() {
  const { status: modelStatus, error: modelError, detect } = useFaceLandmarker()

  const camera = useCamera()
  const [isCameraOpen, setIsCameraOpen] = useState(false)

  const photoInputRef = useRef(null)
  const [photo, setPhoto] = useState(null)
  const [placement, setPlacement] = useState(null)
  const [photoError, setPhotoError] = useState(null)

  const [selectedFrameId, setSelectedFrameId] = useState(null)

  const [scale, setScale] = useState(1)
  const [verticalOffset, setVerticalOffset] = useState(0)
  const [rotationOffsetDegrees, setRotationOffsetDegrees] = useState(0)

  // Solo los marcos disponibles y con foto: sin foto no hay nada que superponer.
  const framesQuery = useQuery({
    queryKey: ['frames', 'AVAILABLE'],
    queryFn: () => framesApi.list('AVAILABLE'),
  })
  const frames = (framesQuery.data ?? []).filter((frame) => frame.imageOriginalName)

  /**
   * Único punto por el que entra una foto, venga de la cámara o de un archivo: detecta el
   * rostro y deja calculada la posición del marco.
   *
   * La foto del beneficiario no sale del navegador en ningún caso: se procesa acá y no se sube
   * a ningún lado (Ley 25.326, RNF-02).
   */
  async function applyPhoto(source) {
    setPhotoError(null)
    try {
      const image = await loadImage(source)
      const landmarks = detect(image)
      setPhoto(image)
      if (!landmarks) {
        setPlacement(null)
        setPhotoError(
          'No se detectó ningún rostro. Probá de frente, con buena luz y sin nada que tape la cara.',
        )
        return
      }
      setPlacement(computeFramePlacement(landmarks, image.width, image.height))
    } catch (caught) {
      setPhotoError(caught.message)
    }
  }

  async function handlePhotoChange(event) {
    const file = event.target.files[0]
    event.target.value = ''
    if (!file) return

    if (!file.type.startsWith('image/')) {
      setPhotoError('El archivo tiene que ser una foto')
      return
    }
    if (file.size > MAX_PHOTO_BYTES) {
      setPhotoError('La foto supera los 10 MB')
      return
    }

    const objectUrl = URL.createObjectURL(file)
    try {
      await applyPhoto(objectUrl)
    } finally {
      URL.revokeObjectURL(objectUrl)
    }
  }

  async function handleCapture(dataUrl) {
    camera.stop()
    setIsCameraOpen(false)
    await applyPhoto(dataUrl)
  }

  function openCamera() {
    setIsCameraOpen(true)
    camera.start()
  }

  function closeCamera() {
    camera.stop()
    setIsCameraOpen(false)
  }

  /**
   * La URL firmada vence, así que se pide al elegir el marco y no se cachea (`gcTime: 0`).
   * `placeholderData` deja dibujado el marco anterior mientras carga el nuevo, para que el
   * canvas no parpadee al pasar de uno a otro.
   */
  const frameImageQuery = useQuery({
    queryKey: ['frames', selectedFrameId, 'image'],
    queryFn: async () => {
      const { url } = await framesApi.getImage(selectedFrameId)
      return loadImage(url)
    },
    enabled: Boolean(selectedFrameId),
    gcTime: 0,
    placeholderData: (previous) => previous,
  })
  const frameImage = frameImageQuery.data ?? null
  const frameError = frameImageQuery.error?.message

  if (modelStatus === 'unavailable') {
    return (
      <Layout>
        <h2 className="text-xl font-semibold text-slate-900">Probador virtual</h2>
        <div className="mt-6">
          <Alert>
            El probador no está disponible en este dispositivo: no se pudo cargar el detector de
            rostros ({modelError}). El resto del sistema funciona normalmente.
          </Alert>
        </div>
      </Layout>
    )
  }

  return (
    <Layout>
      <div>
        <h2 className="text-xl font-semibold text-slate-900">Probador virtual</h2>
        <p className="mt-1 text-sm text-slate-500">
          Sacale una foto de frente al beneficiario, con la cámara o desde un archivo, y probá cómo
          le quedan los marcos disponibles. La foto no se guarda ni se sube: se procesa en este
          mismo dispositivo.
        </p>
      </div>

      {modelStatus === 'loading' && (
        <p className="mt-6 text-slate-500">Cargando el detector de rostros…</p>
      )}

      {modelStatus === 'ready' && (
        <div className="mt-6 grid grid-cols-1 gap-6 lg:grid-cols-3">
          <div className="lg:col-span-2">
            {isCameraOpen ? (
              <CameraCapture camera={camera} onCapture={handleCapture} onCancel={closeCamera} />
            ) : (
              <>
                {photo ? (
                  <TryOnCanvas
                    photo={photo}
                    frameImage={frameImage}
                    placement={placement}
                    scale={scale}
                    verticalOffset={verticalOffset}
                    rotationOffsetDegrees={rotationOffsetDegrees}
                  />
                ) : (
                  <div className="flex h-80 items-center justify-center rounded-lg border border-dashed border-slate-300 bg-white px-6 text-center text-slate-500">
                    Todavía no hay foto cargada.
                  </div>
                )}

                {photoError && (
                  <div className="mt-3">
                    <Alert>{photoError}</Alert>
                  </div>
                )}

                <input
                  ref={photoInputRef}
                  type="file"
                  accept="image/*"
                  onChange={handlePhotoChange}
                  className="hidden"
                />
                <div className="mt-3 flex flex-wrap gap-3">
                  <button
                    type="button"
                    onClick={openCamera}
                    className="rounded-lg bg-sky-700 px-4 py-2.5 font-medium text-white transition hover:bg-sky-800"
                  >
                    {photo ? 'Sacar otra foto' : 'Sacar una foto'}
                  </button>
                  <button
                    type="button"
                    onClick={() => photoInputRef.current?.click()}
                    className="rounded-lg border border-slate-300 bg-white px-4 py-2.5 font-medium text-slate-700 transition hover:bg-slate-50"
                  >
                    Elegir un archivo
                  </button>
                </div>
              </>
            )}
          </div>

          <div className="space-y-6">
            <section>
              <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-500">
                Marcos con foto
              </h3>
              {framesQuery.isError && (
                <div className="mt-3">
                  <Alert>{framesQuery.error.message}</Alert>
                </div>
              )}
              {framesQuery.isSuccess && frames.length === 0 && (
                <p className="mt-3 rounded-lg border border-dashed border-slate-300 bg-white px-4 py-6 text-center text-sm text-slate-500">
                  Ningún marco disponible tiene foto cargada todavía. Se suben desde el inventario.
                </p>
              )}
              <ul className="mt-3 space-y-2">
                {frames.map((frame) => (
                  <li key={frame.id}>
                    <button
                      type="button"
                      onClick={() => setSelectedFrameId(frame.id)}
                      className={`w-full rounded-lg border px-4 py-3 text-left text-sm transition ${
                        selectedFrameId === frame.id
                          ? 'border-sky-500 bg-sky-50'
                          : 'border-slate-200 bg-white hover:border-sky-300'
                      }`}
                    >
                      <span className="font-medium text-slate-900">{frame.sealCode}</span>
                      <span className="mt-0.5 block text-slate-500">
                        {FRAME_TYPE_LABELS[frame.frameType] ?? frame.frameType} ·{' '}
                        {FRAME_MATERIAL_LABELS[frame.material] ?? frame.material}
                      </span>
                    </button>
                  </li>
                ))}
              </ul>
              {frameError && (
                <div className="mt-3">
                  <Alert>{frameError}</Alert>
                </div>
              )}
            </section>

            {placement && frameImage && (
              <section>
                <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-500">
                  Ajuste fino
                </h3>
                <p className="mt-1 text-sm text-slate-500">
                  El encuadre automático acierta el eje y la inclinación; cuánto baja sobre la nariz
                  depende de cada cara.
                </p>
                <label className="mt-3 block text-sm text-slate-600">
                  Tamaño
                  <input
                    type="range"
                    min="0.6"
                    max="1.4"
                    step="0.01"
                    value={scale}
                    onChange={(event) => setScale(Number(event.target.value))}
                    className="mt-1 w-full"
                  />
                </label>
                <label className="mt-3 block text-sm text-slate-600">
                  Altura
                  <input
                    type="range"
                    min="-0.6"
                    max="0.6"
                    step="0.01"
                    value={verticalOffset}
                    onChange={(event) => setVerticalOffset(Number(event.target.value))}
                    className="mt-1 w-full"
                  />
                </label>
                <label className="mt-3 block text-sm text-slate-600">
                  Rotación
                  <input
                    type="range"
                    min="-20"
                    max="20"
                    step="1"
                    value={rotationOffsetDegrees}
                    onChange={(event) => setRotationOffsetDegrees(Number(event.target.value))}
                    className="mt-1 w-full"
                  />
                </label>
              </section>
            )}
          </div>
        </div>
      )}
    </Layout>
  )
}
