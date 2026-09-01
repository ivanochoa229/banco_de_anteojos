import { useState } from 'react'
import { appointmentsApi } from '../../../api/appointmentsApi'

/**
 * La URL firmada vence, así que se pide recién al hacer clic y no se guarda en ningún lado.
 * La pestaña se abre antes del await: si se abriera después, el navegador la bloquearía por
 * no venir de una interacción directa del usuario.
 */
export function AppointmentReceiptLink({ appointmentId, fileName }) {
  const [isOpening, setIsOpening] = useState(false)
  const [error, setError] = useState(null)

  async function handleClick() {
    setError(null)
    setIsOpening(true)
    const tab = window.open('', '_blank')
    if (tab) tab.opener = null
    try {
      const { url } = await appointmentsApi.getReceipt(appointmentId)
      if (tab) tab.location = url
      else window.location.assign(url)
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
        className="text-xs font-medium text-sky-700 hover:underline disabled:text-slate-400 disabled:no-underline"
      >
        {isOpening ? 'Abriendo…' : `Ver comprobante (${fileName})`}
      </button>
      {error && <p className="mt-1 text-xs text-red-600">{error}</p>}
    </div>
  )
}
