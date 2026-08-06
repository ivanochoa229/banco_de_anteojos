import { useState } from 'react'
import { applicantsApi } from '../../../api/applicantsApi'

/**
 * La presigned URL viene con el registro pero vence a los pocos minutos, así que la pantalla
 * puede quedar abierta con una URL muerta. Se vuelve a pedir el certificado al hacer clic y se
 * abre la URL fresca. La pestaña se abre antes del await: si se abriera después, el navegador
 * la bloquearía por no venir de una interacción directa del usuario.
 */
export function AnsesCertificateFileLink({ applicantId, fileName }) {
  const [isOpening, setIsOpening] = useState(false)
  const [error, setError] = useState(null)

  async function handleClick() {
    setError(null)
    setIsOpening(true)
    const tab = window.open('', '_blank')
    if (tab) tab.opener = null
    try {
      const { fileUrl } = await applicantsApi.getAnsesCertificate(applicantId)
      if (tab) tab.location = fileUrl
      else window.location.assign(fileUrl)
    } catch (caught) {
      tab?.close()
      setError(caught.message)
    } finally {
      setIsOpening(false)
    }
  }

  return (
    <div>
      <button
        type="button"
        onClick={handleClick}
        disabled={isOpening}
        className="font-medium text-sky-700 hover:underline disabled:text-slate-400 disabled:no-underline"
      >
        {isOpening ? 'Abriendo…' : fileName}
      </button>
      {error && <p className="mt-1 text-sm text-red-600">{error}</p>}
    </div>
  )
}
