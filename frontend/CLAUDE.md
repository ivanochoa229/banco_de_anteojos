# CLAUDE.md — Frontend (Banco de Anteojos)

> Convenciones del frontend. Alinear siempre con el backend (rutas, formato de datos, auth) y con
> los requerimientos en `../docs/REQUIREMENTS.md`.

## Stack

React + Tailwind CSS, bundler **Vite**. Router: React Router. Estado de servidor: TanStack Query
(o `fetch` + hooks propios). Testing: Jest + React Testing Library. Probador virtual: MediaPipe
Face Mesh + Canvas API (2D sobre foto estática).

**Idioma del dominio y la UI: español** (mismo vocabulario que el backend: Solicitante, Marco, Turno,
Donante). Código en inglés como es habitual.

## Estructura (feature-based, espeja los dominios del backend)

```
src/
├── api/                 # un cliente HTTP por dominio (solicitantesApi, marcosApi, turnosApi...)
├── features/{dominio}/  # solicitantes, donantes, marcos, asignacion, turnos, envios, catalogo,
│   ├── components/      #   indicadores, probador  — cada uno con sus componentes, hooks y páginas
│   ├── hooks/
│   ├── pages/
│   └── types.ts
├── components/          # UI compartida (Button, Input, Table, Layout)
├── context/             # AuthContext (JWT + rol del usuario)
├── routes/              # definición de rutas + guards por rol
├── lib/                 # fetch wrapper, formato de fechas, helpers
└── styles/
```

## Consumo de la API (coincidir con el backend)

- Base URL desde `import.meta.env.VITE_API_URL`. Nunca hardcodear la URL.
- Todas las rutas versionadas: `/v1/...`. JSON en **camelCase** (igual que el backend).
- Auth por header `Authorization: Bearer <token>` en cada request autenticado.
- **Un cliente por dominio en `api/`**; los componentes nunca hacen `fetch` suelto — siempre pasan por
  el cliente del dominio (mismo espíritu que el aislamiento de capas del backend).
- Manejo de errores: el backend devuelve `{ timestamp, status, error, message, path }`. Mostrar
  `message` al usuario; no exponer detalles crudos.

## Autenticación y roles

- Roles `ADMIN` / `OPERADOR` (coinciden con el backend). Guardar el JWT y el rol en `AuthContext`.
- **Guards de ruta por rol**: rutas de administración/configuración solo para `ADMIN`.
- Redirigir a login ante 401; limpiar el token al cerrar sesión.

## Estado

- Estado local con `useState`/`useReducer`. Estado de servidor (listas, detalles) con TanStack Query
  o hooks propios sobre los clientes de `api/`.
- **Sin abstracciones prematuras**: nada de Redux ni stores globales salvo que un caso real lo exija.

## Fechas

Convención única `DD/MM/YYYY (ART)`, coincidiendo con el backend. Formatear en un helper de `lib/`,
no inline en cada componente.

## Probador virtual (feature aislada)

- MediaPipe Face Mesh detecta el rostro sobre una **foto estática**; superposición 2D del marco con
  Canvas. No AR 3D en tiempo real (decisión de alcance).
- Aislar la lógica de MediaPipe/Canvas en su propio hook/componente; que un fallo ahí no rompa el resto
  de la app (envolver la carga del modelo con manejo de error y estado de "no disponible").

## Estilos

- Tailwind por utilidades. Extraer componentes reutilizables (`Button`, `Input`, `Card`) para no repetir
  clases. Evitar CSS suelto salvo casos puntuales.
- Usabilidad (RNF-05): interfaz operable por personal no técnico. Labels claros en español, estados de
  carga/error visibles, formularios con validación en el borde.

## Testing

- Jest + React Testing Library. Testear componentes con lógica y hooks; **mockear el cliente de `api/`**,
  no `fetch` directamente. Naming claro por comportamiento.

## Reglas generales

- Componentes chicos y con una responsabilidad. Nombres de dominio en español, autoexplicativos.
- Validar en los bordes (input del usuario, respuesta de la API), no para escenarios imposibles.
- Sin código "por si acaso": si no se usa, se borra.
