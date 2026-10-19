export function Button({ children, className = '', ...props }) {
  // Si className no define un background explícito, aplicamos el estilo primario naranja
  const hasCustomBg = className.includes('bg-')

  const baseStyle =
    'inline-flex items-center justify-center font-semibold rounded-xl transition-all duration-150 focus:outline-none focus:ring-2 focus:ring-orange-500/30 focus:ring-offset-1 disabled:cursor-not-allowed disabled:opacity-50 disabled:shadow-none'

  const defaultStyle = hasCustomBg
    ? ''
    : 'bg-orange-600 hover:bg-orange-700 text-white shadow-sm hover:shadow active:scale-[0.99] px-4 py-2.5 text-sm'

  return (
    <button className={`${baseStyle} ${defaultStyle} ${className}`} {...props}>
      {children}
    </button>
  )
}


