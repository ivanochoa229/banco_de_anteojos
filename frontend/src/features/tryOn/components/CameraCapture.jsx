import { Alert } from '../../../components/Alert'

/** Vista previa en vivo mientras se encuadra. La foto que se usa es la que se congela acá. */
export function CameraCapture({ camera, onCapture, onCancel }) {
  const { videoRef, status, error, facingMode, flip, capture } = camera

  function handleCapture() {
    const dataUrl = capture()
    if (dataUrl) onCapture(dataUrl)
  }

  return (
    <div>
      <div className="overflow-hidden rounded-lg border border-slate-200 bg-slate-900">
        <video
          ref={videoRef}
          playsInline
          muted
          // Espejada como un espejo real: encuadrarse al revés es desorientante.
          className={`w-full ${facingMode === 'user' ? '-scale-x-100' : ''}`}
        />
      </div>

      {status === 'starting' && <p className="mt-3 text-slate-500">Encendiendo la cámara…</p>}
      {error && (
        <div className="mt-3">
          <Alert>{error}</Alert>
        </div>
      )}

      <div className="mt-3 flex flex-wrap gap-3">
        <button
          type="button"
          onClick={handleCapture}
          disabled={status !== 'streaming'}
          className="rounded-lg bg-sky-700 px-4 py-2.5 font-medium text-white transition hover:bg-sky-800 disabled:cursor-not-allowed disabled:opacity-60"
        >
          Sacar la foto
        </button>
        <button
          type="button"
          onClick={flip}
          disabled={status !== 'streaming'}
          className="rounded-lg border border-slate-300 bg-white px-4 py-2.5 font-medium text-slate-700 transition hover:bg-slate-50 disabled:opacity-60"
        >
          Cambiar de cámara
        </button>
        <button
          type="button"
          onClick={onCancel}
          className="px-2 py-2.5 text-sm font-medium text-slate-500 hover:underline"
        >
          Cancelar
        </button>
      </div>
    </div>
  )
}
