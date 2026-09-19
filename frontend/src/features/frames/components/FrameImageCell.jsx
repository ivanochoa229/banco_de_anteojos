import { useRef, useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { framesApi } from '../../../api/framesApi'
import { useBackgroundRemoval } from '../hooks/useBackgroundRemoval'
import { FrameImageEditor } from './FrameImageEditor'

const MAX_FILE_BYTES = 10 * 1024 * 1024

function validateFile(file) {
  if (!file.type.startsWith('image/')) return 'Elegí un archivo de imagen'
  if (file.size > MAX_FILE_BYTES) return 'El archivo supera los 10 MB'
  return null
}

/**
 * La foto del marco es lo que el probador virtual superpone sobre la cara (RF-18), así que
 * necesita el fondo recortado. El recorte corre en el navegador (useBackgroundRemoval) antes
 * de subir: acepta cualquier imagen (foto de cámara o archivo) y siempre sube un PNG con canal
 * alfa. Si el recorte falla no se sube nada — el marco queda sin foto, no bloquea la carga.
 *
 * El recorte automático no siempre perfora bien el hueco del cristal (marcos oscuros, sombra
 * del propio vidrio): antes de subir se muestra un editor con pincel de borrado para que el
 * operador termine de limpiarlo a mano.
 */
export function FrameImageCell({ frame }) {
  const queryClient = useQueryClient()
  const inputRef = useRef(null)
  const [validationError, setValidationError] = useState(null)
  const [editingFile, setEditingFile] = useState(null)
  const { isProcessing, process: removeBackground } = useBackgroundRemoval()

  const uploadMutation = useMutation({
    mutationFn: (file) => framesApi.uploadImage(frame.id, file),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['frames'] }),
  })

  async function handleChange(event) {
    const file = event.target.files[0]
    event.target.value = ''
    if (!file) return

    const error = validateFile(file)
    setValidationError(error)
    if (error) return

    try {
      const cutOut = await removeBackground(file)
      setEditingFile(cutOut)
    } catch (removalError) {
      setValidationError(removalError.message)
    }
  }

  function handleEditorConfirm(blob) {
    setEditingFile(null)
    uploadMutation.mutate(new File([blob], 'marco.png', { type: 'image/png' }))
  }

  const isBusy = isProcessing || uploadMutation.isPending
  const error = validationError ?? uploadMutation.error?.message

  return (
    <div>
      {frame.imageOriginalName ? (
        <span className="text-slate-600">{frame.imageOriginalName}</span>
      ) : (
        <span className="text-slate-400">Sin foto</span>
      )}

      {/* capture="environment" hace que el celular ofrezca cámara o galería; en desktop es
          un input de archivo común. */}
      <input
        ref={inputRef}
        type="file"
        accept="image/*"
        capture="environment"
        onChange={handleChange}
        className="hidden"
      />
      <button
        type="button"
        onClick={() => inputRef.current?.click()}
        disabled={isBusy}
        className="mt-1 block text-sm font-medium text-slate-500 hover:text-slate-700 hover:underline disabled:text-slate-300 disabled:no-underline"
      >
        {isProcessing
          ? 'Recortando fondo…'
          : uploadMutation.isPending
            ? 'Subiendo…'
            : frame.imageOriginalName
              ? 'Reemplazar'
              : 'Subir'}
      </button>

      {error && <p className="mt-1 text-sm text-red-600">{error}</p>}

      {editingFile && (
        <FrameImageEditor
          imageFile={editingFile}
          onConfirm={handleEditorConfirm}
          onCancel={() => setEditingFile(null)}
        />
      )}
    </div>
  )
}
