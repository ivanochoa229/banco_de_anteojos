import { useRef, useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { framesApi } from '../../../api/framesApi'

// Mismos formatos que acepta el backend: solo los que tienen canal alfa.
const ALLOWED_FILE_TYPES = ['image/png', 'image/webp']
const MAX_FILE_BYTES = 10 * 1024 * 1024

function validateFile(file) {
  if (!ALLOWED_FILE_TYPES.includes(file.type)) {
    return 'La foto debe ser PNG o WEBP con el fondo recortado'
  }
  if (file.size > MAX_FILE_BYTES) return 'El archivo supera los 10 MB'
  return null
}

/** La foto del marco es lo que el probador virtual superpone sobre la cara (RF-18). */
export function FrameImageCell({ frame }) {
  const queryClient = useQueryClient()
  const inputRef = useRef(null)
  const [validationError, setValidationError] = useState(null)

  const uploadMutation = useMutation({
    mutationFn: (file) => framesApi.uploadImage(frame.id, file),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['frames'] }),
  })

  function handleChange(event) {
    const file = event.target.files[0]
    event.target.value = ''
    if (!file) return

    const error = validateFile(file)
    setValidationError(error)
    if (!error) uploadMutation.mutate(file)
  }

  const error = validationError ?? uploadMutation.error?.message

  return (
    <div>
      {frame.imageOriginalName ? (
        <span className="text-slate-600">{frame.imageOriginalName}</span>
      ) : (
        <span className="text-slate-400">Sin foto</span>
      )}

      <input
        ref={inputRef}
        type="file"
        accept="image/png,image/webp"
        onChange={handleChange}
        className="hidden"
      />
      <button
        type="button"
        onClick={() => inputRef.current?.click()}
        disabled={uploadMutation.isPending}
        className="mt-1 block text-sm font-medium text-slate-500 hover:text-slate-700 hover:underline disabled:text-slate-300 disabled:no-underline"
      >
        {uploadMutation.isPending ? 'Subiendo…' : frame.imageOriginalName ? 'Reemplazar' : 'Subir'}
      </button>

      {error && <p className="mt-1 text-sm text-red-600">{error}</p>}
    </div>
  )
}
