import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    // En dev las requests a /v1 van al backend local; en prod VITE_API_URL trae la URL completa.
    proxy: {
      '/v1': 'http://localhost:8080',
    },
  },
})
