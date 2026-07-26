export function Button({ children, className = '', ...props }) {
  return (
    <button
      className={`rounded-lg bg-sky-700 px-4 py-2.5 font-medium text-white transition hover:bg-sky-800 disabled:cursor-not-allowed disabled:opacity-60 ${className}`}
      {...props}
    >
      {children}
    </button>
  )
}
