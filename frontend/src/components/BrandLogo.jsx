export function HacerFuturoLogo({ className = 'h-10 w-auto', theme = 'light' }) {
  const src =
    theme === 'dark'
      ? '/images/logo_hacer_futuro_white.png'
      : '/images/logo_hacer_futuro_trans.png'

  return (
    <img
      src={src}
      alt="Fundación Hacer Futuro"
      className={`shrink-0 object-contain ${className}`}
    />
  )
}

export function BancoAnteojosLogo({ className = 'h-12 w-auto', variant = 'full' }) {
  return (
    <img
      src="/images/logo_banco_anteojos_trans.png"
      alt="Banco de Anteojos · Fundación Hacer Futuro"
      className={`shrink-0 object-contain ${className}`}
    />
  )
}
