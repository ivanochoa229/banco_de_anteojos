import { useEffect, useRef, useState } from 'react'
import { Input } from './Input'

const DEBOUNCE_MS = 300

/**
 * Buscador de un listado. Avisa el término recién cuando se deja de tipear, para no mandar un
 * pedido a la API por cada letra. `value` es el valor inicial (el que ya está en la URL).
 */
export function SearchInput({ id, label, placeholder, value, onSearch }) {
  const [text, setText] = useState(value)
  const timer = useRef(null)

  useEffect(() => () => clearTimeout(timer.current), [])

  function handleChange(event) {
    const next = event.target.value
    setText(next)
    clearTimeout(timer.current)
    timer.current = setTimeout(() => onSearch(next.trim()), DEBOUNCE_MS)
  }

  return (
    <Input
      id={id}
      type="search"
      label={label}
      placeholder={placeholder}
      value={text}
      onChange={handleChange}
    />
  )
}
