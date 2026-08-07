import { cp } from 'node:fs/promises'
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import basicSsl from '@vitejs/plugin-basic-ssl'

/**
 * `npm run dev:https` levanta el dev server con TLS y escuchando en toda la red local.
 *
 * Es solo para probar el probador virtual desde el celular: la cámara (`getUserMedia`) exige
 * contexto seguro, y entrar por la IP en HTTP no lo es. El certificado es autofirmado, así que
 * el celular avisa "sitio no seguro" la primera vez y hay que aceptar. En el día a día se sigue
 * usando `npm run dev`, en HTTP, que en localhost ya cuenta como contexto seguro.
 */
const useHttps = process.env.HTTPS === 'true'

const MEDIAPIPE_WASM_SOURCE = 'node_modules/@mediapipe/tasks-vision/wasm'
const MEDIAPIPE_WASM_TARGET = 'public/mediapipe/wasm'

/**
 * El runtime wasm de MediaPipe pesa 34 MB: no se versiona. Se copia de node_modules a public/
 * al arrancar, así lo sirve tanto el dev server como el build, sin pedirle nada a un CDN
 * (RNF-04: el probador no depende de un servidor de procesamiento externo).
 */
function copyMediapipeWasm() {
  return {
    name: 'copy-mediapipe-wasm',
    async buildStart() {
      await cp(MEDIAPIPE_WASM_SOURCE, MEDIAPIPE_WASM_TARGET, { recursive: true })
    },
  }
}

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss(), copyMediapipeWasm(), ...(useHttps ? [basicSsl()] : [])],
  server: {
    // Con HTTPS escucha en toda la red local para poder entrar desde el celular.
    host: useHttps,
    // En dev las requests a /v1 van al backend local; en prod VITE_API_URL trae la URL completa.
    // El proxy lo resuelve el server de Vite, así que el backend sigue en HTTP sin mixed content.
    proxy: {
      '/v1': 'http://localhost:8080',
    },
  },
})
