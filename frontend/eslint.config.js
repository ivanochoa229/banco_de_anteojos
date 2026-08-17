import js from '@eslint/js'
import globals from 'globals'
import reactHooks from 'eslint-plugin-react-hooks'
import reactRefresh from 'eslint-plugin-react-refresh'
import { defineConfig, globalIgnores } from 'eslint/config'

export default defineConfig([
  // public/mediapipe no es código nuestro: es el runtime wasm que vite.config.js copia desde
  // node_modules en cada build (por eso está gitignoreado). Lintearlo tapaba los errores
  // reales con más de mil hallazgos de un bundle minificado.
  globalIgnores(['dist', 'public/mediapipe']),
  {
    files: ['**/*.{js,jsx}'],
    extends: [
      js.configs.recommended,
      reactHooks.configs.flat.recommended,
      reactRefresh.configs.vite,
    ],
    languageOptions: {
      globals: globals.browser,
      parserOptions: { ecmaFeatures: { jsx: true } },
    },
  },
  // Los archivos de configuración corren en Node, no en el navegador: usan process y las APIs
  // de node: para copiar el wasm.
  {
    files: ['*.config.js'],
    languageOptions: { globals: globals.node },
  },
])
