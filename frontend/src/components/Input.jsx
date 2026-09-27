export function Input({ id, label, error, ...props }) {
  return (
    <div>
      {label && (
        <label htmlFor={id} className="mb-1.5 block text-sm font-semibold text-slate-700">
          {label}
        </label>
      )}
      <input
        id={id}
        className={`w-full rounded-xl border bg-white px-3.5 py-2.5 text-sm text-slate-900 shadow-xs outline-none transition-all placeholder:text-slate-400 focus:ring-3 ${
          error
            ? 'border-red-400 focus:border-red-500 focus:ring-red-100'
            : 'border-slate-300 focus:border-orange-500 focus:ring-orange-500/15'
        }`}
        {...props}
      />
      {error && <p className="mt-1.5 text-xs font-medium text-red-600">{error}</p>}
    </div>
  )
}

