// options: [{ value, label }]. placeholder agrega una opción vacía inicial.
export function Select({ id, label, error, options, placeholder, ...props }) {
  return (
    <div>
      {label && (
        <label htmlFor={id} className="mb-1.5 block text-sm font-semibold text-slate-700">
          {label}
        </label>
      )}
      <select
        id={id}
        className={`w-full rounded-xl border bg-white px-3.5 py-2.5 text-sm text-slate-900 shadow-xs outline-none transition-all focus:ring-3 ${
          error
            ? 'border-red-400 focus:border-red-500 focus:ring-red-100'
            : 'border-slate-300 focus:border-orange-500 focus:ring-orange-500/15'
        }`}
        {...props}
      >
        {placeholder && <option value="">{placeholder}</option>}
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
      {error && <p className="mt-1.5 text-xs font-medium text-red-600">{error}</p>}
    </div>
  )
}

