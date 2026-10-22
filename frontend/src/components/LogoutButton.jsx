import { useEffect, useState } from 'react'
import { createPortal } from 'react-dom'
import { useAuth } from '../context/useAuth'
import { Button } from './Button'

/**
 * "Cerrar sesión" pide confirmación: el botón queda pegado a la navegación y un clic de más
 * saca al operador en medio de una carga.
 *
 * El diálogo va por portal a body: los headers usan backdrop-blur, que convierte al header en
 * el contenedor de cualquier `fixed` de adentro y lo dejaría recortado ahí.
 */
export function LogoutButton({ className, onLogout }) {
  const { logout } = useAuth()
  const [isConfirming, setIsConfirming] = useState(false)

  useEffect(() => {
    if (!isConfirming) return undefined
    function handleKeyDown(event) {
      if (event.key === 'Escape') setIsConfirming(false)
    }
    document.addEventListener('keydown', handleKeyDown)
    return () => document.removeEventListener('keydown', handleKeyDown)
  }, [isConfirming])

  function confirmLogout() {
    setIsConfirming(false)
    onLogout?.()
    logout()
  }

  return (
    <>
      <button type="button" onClick={() => setIsConfirming(true)} className={className}>
        Cerrar sesión
      </button>

      {isConfirming &&
        createPortal(
          <div
            className="fixed inset-0 z-[60] flex items-center justify-center bg-slate-900/40 p-4"
            onClick={() => setIsConfirming(false)}
          >
            <div
              role="dialog"
              aria-modal="true"
              aria-labelledby="logout-dialog-title"
              className="w-full max-w-sm rounded-2xl bg-white p-6 shadow-xl"
              onClick={(event) => event.stopPropagation()}
            >
              <h2 id="logout-dialog-title" className="text-lg font-semibold text-slate-900">
                ¿Cerrar sesión?
              </h2>
              <p className="mt-2 text-sm text-slate-600">
                Vas a tener que volver a ingresar con tu correo y contraseña.
              </p>
              <div className="mt-6 flex justify-end gap-3">
                <Button
                  autoFocus
                  onClick={() => setIsConfirming(false)}
                  className="border border-slate-200 bg-white px-4 py-2.5 text-sm text-slate-700 hover:bg-slate-50"
                >
                  Cancelar
                </Button>
                <Button onClick={confirmLogout}>Cerrar sesión</Button>
              </div>
            </div>
          </div>,
          document.body,
        )}
    </>
  )
}
