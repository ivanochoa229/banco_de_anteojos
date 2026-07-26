# CLAUDE.md — Banco de Anteojos (Fundación Hacer Futuro)

> Contexto del proyecto para Claude Code. Se carga automáticamente al inicio de cada sesión.
> Proyecto Final UTN-FRT 2026 · ID T045.

## Qué es este proyecto

Sistema de información web para digitalizar el **Banco de Anteojos** de la Fundación Hacer Futuro
(Chacabuco 27, San Miguel de Tucumán). La fundación recibe marcos donados, les coloca cristales
graduados y los entrega gratis a personas sin cobertura médica. Hoy opera en papel, sin trazabilidad
ni inventario digital. El sistema digitaliza recepción de donaciones, validación de elegibilidad de
beneficiarios, inventario con match receta↔marco, trazabilidad end-to-end, turnos, logística de
envíos, probador virtual y catálogo de anteojos de sol.

**Idioma del código: inglés.** Todo el código en inglés: entidades, DTOs, endpoints, variables,
métodos, tablas y columnas (`Applicant`, `Frame`, `Appointment`, `Donor`). En español quedan
únicamente: los mensajes al usuario final (incluidos los errores de la API), los comentarios,
los commits y la documentación.

## Estructura del repositorio (monorepo)

```
banco-anteojos/
├── backend/          # Spring Boot + Maven — ver backend/CLAUDE.md para convenciones
├── frontend/         # React + Tailwind (Vite) — ver frontend/CLAUDE.md
├── docs/             # Documentación de cátedra (EDT, actas, cronograma)
└── CLAUDE.md         # este archivo
```

Deploy en Render: backend y frontend se despliegan desde este mismo repo apuntando a
`backend/` y `frontend/` como Root Directory.

## Stack

| Capa | Tecnología |
|---|---|
| Backend | Java 17 + Spring Boot + Maven (wrapper `./mvnw`) |
| Seguridad | Spring Security + JWT + BCrypt · roles `ADMIN` / `OPERADOR` |
| Frontend | React + Tailwind CSS (Vite) |
| Base de datos | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| Migraciones | Flyway (schema versionado; Hibernate DDL en `none`) |
| Storage imágenes | Cloudflare R2 (S3-compatible) — solo se guarda la URL en PostgreSQL |
| Hosting | Render |

## Integraciones externas — decisiones ya tomadas (no reabrir sin avisar)

- **RENAPER (SID):** validación de identidad vía Convenio de Confronte de Datos por TAD (gratuito).
  El convenio es un trámite administrativo externo, **no una feature del sistema**. Fallback:
  validación presencial del DNI por el operador.
- **ANSES (negativa):** NO hay API. Se valida leyendo el **código de barras del PDF** (codifica
  `CUIL-NroTransaccion`). El sistema chequea: CUIL coincide con el solicitante + vigencia ≤ 30 días.
  Es procesamiento **local**, no un cliente de tercero. Adulteración no detectable → validación final
  por el operador.
- **17TRACK (logística Vía Cargo):** flujo `/register` para registrar envío + **webhook push** para
  actualizaciones. Tier gratuito (100 envíos/mes). Se descartó pegarle directo a Vía Cargo (reCAPTCHA v3).
- **Probador virtual:** MediaPipe Face Mesh + superposición 2D con Canvas sobre foto estática
  (NO AR 3D en tiempo real). Elegido por compatibilidad con gama baja. El 3D queda como mejora futura.
- **Cloudflare R2:** object storage S3-compatible. Solo la URL va a PostgreSQL, nunca el binario.

## Dominios del sistema (backend)

security (seguridad) · applicants (solicitantes/beneficiarios) · donors (donantes) · frames
(inventario de marcos) · assignments (match receta↔marco + trazabilidad) · appointments (turnos) ·
shipments (logística/17TRACK) · catalog (venta anteojos de sol) · indicators (panel de impacto).
Clientes de terceros: RENAPER, 17TRACK, Cloudflare R2, notificaciones.

## Flujo de trabajo del equipo

- Equipo de 4: Ocaranza (frontend/UI/MediaPipe), Ochoa (backend/seguridad), Ponce (backend/logística/DevOps),
  Sleiman (análisis/documentación/storage).
- **Ramas + PRs.** `main` con branch protection; nada se mergea directo a `main`.
- Commits en español, descriptivos. Un PR = un cambio coherente.
- Antes de dar por terminada una feature: que compile (`./mvnw test` en backend, `npm run build` en front)
  y que pasen los tests.

## Reglas para Claude Code

- **Antes de escribir backend, leé `backend/CLAUDE.md`** — ahí están las convenciones de arquitectura
  (capas, aislamiento entre dominios, REST, testing) que son de cumplimiento estricto.
- No inventes integraciones ni cambies decisiones técnicas ya tomadas (ver sección de integraciones).
- Sin abstracciones prematuras: preferí código directo y claro sobre capas hipotéticas.
- Mostrá el plan antes de cambios grandes (crear módulos, tocar el schema, agregar dependencias).
