# CLAUDE.md — Backend (Banco de Anteojos)

> Convenciones de arquitectura del backend. Cumplimiento estricto.
> Referencia completa y detallada: `docs/standards/ARCHITECTURE_GUIDE.md` — leerla ante cualquier duda
> de patrón. Este archivo resume lo no-negociable y lo adapta a este dominio.

## Stack backend

Java 17 · Spring Boot · Maven (usar siempre el wrapper `./mvnw`, ejecutar vía bash / Git Bash en Windows;
no usar `-q` en builds automatizados) · PostgreSQL + Flyway (Hibernate DDL en `none`, el schema lo
gestiona Flyway) · JWT stateless · JUnit 5 + Mockito + AssertJ + MockMvc.

Variables sensibles (JWT secret, credenciales R2, API key de 17TRACK) desde `.env` como system
properties. **Nunca** hardcodeadas ni versionadas.

**Idioma: código 100% en inglés** (entidades, DTOs, endpoints, variables, métodos, schema de DB).
En español solo: mensajes al usuario final (errores de API incluidos), comentarios, commits y docs.

## Arquitectura en 4 capas (+ terceros)

```
presentation/controllers/      → REST, delega al orchestrator, CERO lógica de negocio
orchestrator/                  → coordina services, valida pertenencia ANTES de todo lo demás
business/{dominio}/            → lógica de negocio real, un paquete por dominio
persistence/                   → repositorios JPA
thirdPartyServiceComunication/ → clientes de APIs externas (RENAPER, 17TRACK, R2, notificaciones)
```

Dominios de este proyecto: `security`, `applicants`, `donors`, `frames`, `assignments`,
`appointments`, `shipments`, `catalog`, `indicators`.

### Reglas críticas (no romper)

- **Controllers sin lógica de negocio.** Reciben el request, delegan al orchestrator, devuelven la
  respuesta. Un paquete por entidad principal.
- **Identificadores siempre en `@PathVariable`, nunca en el body.**
- **Orchestrator sin lógica de negocio** (nada de loops, filtros, cálculos). Solo coordina. Interfaz
  `*UseCaseOrchestrator` + impl `*UseCaseHandler`.
- **Validación de pertenencia como PRIMER paso** de cada caso de uso del orchestrator (que el solicitante/
  marco/turno pertenezca a quien corresponde según el rol). Si falla → excepción de dominio → HTTP 403.
- **Aislamiento entre dominios (regla dura):** un `*ServiceHandler` solo puede inyectar su propio
  repositorio, un `*Helper` del mismo dominio, clientes de terceros y utilidades (JSON, logger).
  **Nunca** el repositorio/servicio de otro dominio, ni otro `*ServiceHandler`. La coordinación
  cross-domain vive en el orchestrator, que pasa los datos ya resueltos como parámetro.
- **Entidades sin setters:** mutación por métodos con intención de negocio
  (`frame.markAsDelivered()`, no `setStatus("DELIVERED")`), o query JPQL de update directa.
- **Operaciones no críticas** (notificaciones, logging) envueltas en try-catch: que un fallo ahí no
  tumbe el flujo principal.
- Naming de servicios: `*ServiceHandler` (no `*ServiceImpl`). Repositorios:
  `*PostgresSqlRepository extends JpaRepository<Entidad, Long>`. Queries complejas como `@Query`, no en el service.

### Clientes de terceros

Un cliente por integración, aislado. El resto del código nunca llama directo a un SDK/HTTP externo.
- `RenaperClient` — validación de identidad (SID).
- `TrackingClient` — 17TRACK (`/register` + recibir webhook push).
- `R2StorageClient` — subir imagen, devolver URL.
- (Notificaciones si aplica.)

> La verificación ANSES **no** es un cliente de terceros: es lectura local del código de barras del PDF.
> Va en `business/applicants` (o un `business/validation` dedicado), no en la capa de terceros.

## REST

- Recursos en **plural**, sin verbos en el path. La acción la da el método HTTP.
  `DELETE /v1/frames/{id}`, no `POST /deleteFrame/{id}`.
- Rutas jerárquicas para relaciones: `GET /v1/applicants/{id}/appointments`.
- Versionado en la URL (`/v1/`). JSON en `camelCase`. Auth por `Authorization: Bearer <token>`.
- Errores estructurados y consistentes (timestamp, status, error, message, path). **Sin filtrar detalles
  internos** (nada de stacktraces ni nombres de tablas/columnas en la respuesta).

## Base de datos (Flyway)

- `snake_case`; tablas en plural, columnas en singular. PK siempre `id`, FK `<entidad>_id`.
- Timestamps de instante `_at` (`TIMESTAMP WITHOUT TIME ZONE`); fechas puras `_date` (`DATE`).
- Booleanos con prefijo `is_`/`has_`, `NOT NULL` + `DEFAULT` coherente.
- Enums/estados como `VARCHAR(n)` con `CHECK` restringiendo valores (ej. estado de un marco, estado de turno).
- Migraciones Flyway = única fuente de verdad del schema. Hibernate no genera DDL.
- Migración ya en `main` es **append-only** (no editarla; crear una nueva).

## Reparto DB ↔ backend

Por defecto: la DB hace lo mínimo (SELECT con WHERE/JOIN indexados, COUNT/SUM/GROUP BY simples,
listas planas). El backend hace merges, ratios (con división por cero explícita), redondeos, defaults,
ordenamientos compuestos y armado de DTOs. Razón: queries chicas son testeables e indexables; la lógica
en Java es unit-testeable sin levantar la DB.

## Testing

Pirámide: ~70–80% unit, 15–20% integración, 5–10% E2E. Tags `@Tag("unit")` / `@Tag("integration")`.
- Test de controller → mockear orchestrator. Orchestrator → mockear services. Service → mockear helper
  y repository.
- Naming: clase `{Entidad}{Capa}Test`; método `[Método]_[Condición]` (`CreateAppointment_Successful`,
  `AssignFrame_WhenNoStock`).
- E2E mínimo por feature: `@SpringBootTest` + `@AutoConfigureMockMvc` + `@Transactional`, pegándole al
  endpoint real y asertando respuesta HTTP **y** estado en DB. Mockear solo clientes de terceros.

## Reglas generales

- Sin abstracciones prematuras (la guía §11): tres líneas repetidas > una abstracción hipotética.
- Validar solo en los bordes (input de usuario, respuesta de API externa), no para escenarios imposibles.
- Comentar el **por qué** (invariantes, workarounds), nunca el qué.

## Nota de escala (importante)

La guía de referencia viene de un backend enterprise (~39 dominios, patrones `*DependsOn*`, auth de cron
con trace IDs, `@Async` en background). **Aplicá siempre** el layering, el aislamiento entre dominios, el
naming, REST, Flyway y testing. Los patrones de escala grande (interfaces `*DependsOn*` formales, cron
asíncrono con trace ID end-to-end) adoptalos **solo cuando el caso real lo pida** — coherente con "sin
abstracciones prematuras". Si dudás si un patrón aplica a un proyecto de 4 personas, preguntá antes de meterlo.
