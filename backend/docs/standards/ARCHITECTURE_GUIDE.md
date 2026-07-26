# Guía de Arquitectura, Prácticas y Convenciones

> Extraída del proyecto Byron Backoffice Service (Spring Boot 3.5 / Java 17) como referencia portable para nuevos proyectos backend. Adaptar nombres de dominio, stack específico y detalles de infraestructura según corresponda.

## 1. Stack de referencia

- **Framework**: Spring Boot (Java 17+)
- **DB**: PostgreSQL + Flyway para migraciones versionadas
- **ORM**: Hibernate/JPA con DDL en modo `none` (el schema lo gestiona Flyway, no Hibernate)
- **Build**: Maven, usando siempre el wrapper commiteado en el repo (`./mvnw`) — versión lockeada, no depende de instalación local. Ejecutar vía bash aunque sea Windows (Git Bash). No usar `-q` en builds automatizados: oculta output y puede parecer un timeout colgado.
- **Auth**: JWT stateless
- **Testing**: JUnit 5 + Mockito + AssertJ + MockMvc, con tags (`@Tag("unit")` / `@Tag("integration")`) para ejecución modular
- **Config**: variables sensibles (JWT secret, credenciales AWS, API keys de integraciones) cargadas desde `.env` como system properties, no hardcodeadas ni versionadas

## 2. Arquitectura en capas

Arquitectura estricta de 4 capas, con una capa transversal de comunicación con terceros:

```
presentation/controllers/      → REST, delega a orchestrators, sin lógica de negocio
orchestrator/                  → coordina services, resuelve dependencias cross-domain
business/{domain}/             → lógica de negocio real, un paquete por dominio
persistence/                   → repositorios JPA
thirdPartyServiceComunication/ → clientes de APIs externas (pagos, storage, notificaciones, etc.)
```

### 2.1 Presentation (controllers)

- Un paquete por entidad principal. Si hay jerarquía entre entidades, replicarla en carpetas (ej. `users/owner/`, `users/staff/`).
- **Cero lógica de negocio** — solo reciben el request, delegan al orchestrator y devuelven la respuesta.
- Un controlador nuevo por cada endpoint que requiera un identificador de entidad *distinta* a la principal (ej. cambiar cuenta entre staff *de un restaurant* → `StaffRestaurantController`, no dentro de `StaffController`).
- Naming: `EntidadController` si opera solo sobre la entidad propia; `EntidadPrincipalEntidadRelacionadaController` si expresa una relación.
- Todos los identificadores de entidad van en `@PathVariable`, **nunca** en el body.

### 2.2 Orchestrator

- Interfaz `*UseCaseOrchestrator` + implementación `*UseCaseHandler`.
- Responsabilidad única: coordinar servicios en el orden correcto, resolver dependencias cross-domain, y ejecutar validaciones de pertenencia (`belongsToUser` o equivalente) **antes de cualquier otra operación**.
- **Cero lógica de negocio**: nada de loops, filtros, cálculos ni constantes de dominio — eso vive en `business/`.

### 2.3 Business (dominio)

- Un paquete por dominio (en el proyecto de referencia, ~39 dominios), cada uno con:
  ```
  business/{domain}/
  ├── entities/
  ├── {Domain}ServiceHandler.java   (implementa *ServiceHandler, no *ServiceImpl)
  ├── {Domain}Helper.java           (lógica auxiliar extraída para testeo aislado)
  ├── dto/request/  (o req/, legacy)
  ├── dto/response/ (o resp/, legacy)
  ├── exception/
  ├── mapper/
  └── validation/
  ```
- **Regla crítica de aislamiento (cross-domain dependency rule)**: un `*ServiceHandler` **solo puede inyectar**:
  - Su propio repositorio (mismo dominio)
  - Un `*Helper` del mismo dominio
  - Clientes de la capa de terceros
  - Utilidades no arquitecturales (mapeadores JSON, loggers)

  **Nunca** puede inyectar el repositorio, servicio o facade de otro dominio. Tampoco un `*ServiceHandler` debe inyectar directamente a otro `*ServiceHandler` de otro dominio — eso rompe el aislamiento; la coordinación pertenece al orchestrator.

  **Flujo cross-domain correcto**: el orchestrator obtiene datos del dominio A a través de una interfaz explícita de dependencia (patrón `*DependsOn*`, ej. `RestaurantDependsOnCheckout`) y los pasa como **parámetro** al servicio del dominio B. El servicio de B nunca hace fetch directo de datos de A — los recibe ya resueltos.

- **Entidades sin setters**: mutación vía métodos semánticos que expresan intención de negocio (`approveProvisioning()`, no `setProvisioningApproved(true)`), o vía queries JPQL de update directas cuando el cambio es puramente de persistencia.
- **Operaciones no críticas** (notificaciones, logging, analytics) envueltas en try-catch para que un fallo ahí no tumbe el flujo principal.
- Jerarquías de entidades con cascada de borrado explícita y documentada (ej. borrar una entidad padre borra en cascada sus hijas directas).

### 2.4 Persistence

- Repositorios `*PostgresSqlRepository extends JpaRepository<Entity, Long>` (o el nombre de tecnología correspondiente si cambia el motor).
- Queries complejas viven acá como `@Query` (JPQL/SQL nativo), no en el servicio.

### 2.5 Third-party communication

- Un cliente por integración externa, aislado del resto de la arquitectura. El resto del código nunca llama directamente a un SDK/HTTP client externo — siempre pasa por este cliente.

## 3. Reparto de procesamiento DB ↔ backend

Principio por defecto: **la DB hace lo mínimo indispensable, el backend procesa**.

**DB hace**:
- `SELECT` con `WHERE`/`JOIN` directos sobre índices
- `COUNT`/`SUM`/`GROUP BY` simples
- Ordenamiento sobre columnas indexadas
- Devuelve listas planas

**Backend hace**:
- Merges entre conjuntos (lookup por mapa)
- Ratios y divisiones (incluyendo manejo explícito de división por cero)
- Redondeos
- Defaults para entradas faltantes
- Ordenamientos compuestos
- Transformación a DTOs de respuesta

**Evitar en queries**: `LEFT JOIN` multi-tabla con `COUNT(DISTINCT ...)` solo para "rellenar ceros", `CASE WHEN` con cálculos numéricos para evitar nulls, expresiones aritméticas que dependan de un valor potencialmente cero (ratios con denominador variable).

**Por qué**: queries chicas son testeables, indexables y reusables; la lógica en el lenguaje de aplicación es unit-testeable sin levantar la DB y más fácil de leer/modificar.

**Excepciones legítimas**: volúmenes muy grandes donde mover datos al backend sería costoso (millones de filas a agregar), o cuando el agregado ya está nativamente soportado por un índice o vista materializada existente.

## 4. Seguridad

- **Auth**: JWT stateless, con un enum de roles (`UserRole` en el proyecto de referencia) y un servicio de contexto (`CurrentUserService.getCurrentUser()`) para obtener el usuario autenticado sin pasarlo por parámetros.
- **Validación de pertenencia obligatoria**: cada caso de uso del orchestrator debe llamar a un servicio de validación (`userValidationService.*BelongsToUser(...)`) como **primer paso**, antes de cualquier otra operación. Si falla, lanza una excepción de dominio (ej. `DoNotBelongsException` → HTTP 403).
- No exponer detalles internos en mensajes de error (stacktraces, nombres de tablas/columnas SQL) — mapear siempre a un error genérico documentado.

### Auth alternativa para endpoints internos/cron

Para endpoints que no representan acciones de un usuario final (jobs internos, triggers de scheduler externo):
- Servicio dedicado de autorización que acepta **o** un secreto compartido vía header custom (ej. `X-Cron-Secret`, comparado contra una env var), **o** un JWT de un rol elevado (super-user).
- Sin validación de pertenencia — el secreto/JWT actúa como la única autenticación.
- Endpoint en `permitAll()` a nivel de framework de seguridad (la auth la hace el controller explícitamente, no el filtro global).
- Response inmediata `202 Accepted`, ejecución real en background (`@Async`), con:
  - Reporte de resultado (éxito/parcial/fallo) enviado por email/canal de alerta solo en el ambiente productivo, según criticidad del job (crítico → siempre reportar; no crítico → solo en error).
  - **Trace ID** propagado end-to-end: generado o tomado de un header (`X-Trace-Id`), guardado en MDC/contexto de logging, devuelto en el response header, y propagado también al thread asíncrono (decorator de MDC en el executor) para poder correlacionar logs de una ejecución completa, incluida la parte en background.
  - `@Async` no puede ser una auto-invocación (los proxies de DI no interceptan llamadas internas del mismo bean) — el método async debe vivir en un bean distinto al que lo invoca, o ser invocado desde otro bean.

## 5. Convenciones REST

- Recursos en **plural**, nunca verbos en el path — la acción la define el método HTTP: `DELETE /users/{id}`, no `POST /deleteUser/{id}`.
- Rutas jerárquicas reflejan relaciones: `GET /users/{userId}/orders`, no `GET /ordersByUserId/{userId}`.
- **Identificadores siempre en path, nunca en body.**
- Métodos HTTP: `GET` leer, `POST` crear, `PUT` update completo (multi-campo), `PATCH` update parcial (un campo), `DELETE` eliminar.
- **PATCH de una sola propiedad**: ruta base + id + nombre de la propiedad (`PATCH /staff/{id}/enabled`).
- **PUT de múltiples campos**: un único endpoint con DTO de campos todos opcionales (los ausentes llegan `null` por deserialización); el repositorio resuelve con `CASE WHEN :param IS NOT NULL THEN :param ELSE columna END` dentro de un `@Query` marcado `@Transactional` + `@Modifying` (sin ambas anotaciones el framework puede interpretar la query como un SELECT). Aplicar este patrón **solo** si distintos casos de uso tocan distintos subconjuntos de campos; si siempre se actualiza todo junto, usar campos requeridos normales.
- Status codes explícitos y completos por endpoint en la documentación (OpenAPI/Swagger): 2xx de éxito, 4xx de error de cliente, 5xx de error de servidor. No declarar 401/403 en endpoints públicos.
- Body vs path vs query: body para datos de creación/actualización, path para identificadores únicos, query para filtrado/paginación/info adicional.
- JSON en `camelCase`.
- Versionado explícito en la URL (`/v1/`, `/v2/`).
- Auth vía header `Authorization: Bearer <token>`, HTTPS obligatorio.
- Mensajes de error estructurados y consistentes (timestamp, status, error, message, path), sin filtrar detalles internos.
- PUT y DELETE deben ser idempotentes por diseño; POST no lo es salvo que se implemente con idempotency keys explícitas.

### Refactor de endpoints legacy

Al migrar un endpoint viejo al estándar nuevo, no reemplazar in-place:
1. Agregar versión a la ruta nueva.
2. Crear un controller nuevo, separado, siguiendo el estándar vigente.
3. Renombrar el controller viejo agregando el sufijo `Legacy` (y su tag de documentación).
4. Si el DTO cambió, mantener ambos (`*DtoLegacy` + nuevo) hasta completar la migración de consumidores.
5. Registrar el refactor en un tracking compartido (ruta vieja → ruta nueva, aclaraciones) para que el equipo sepa qué migrar del lado consumidor.

## 6. Naming de DTOs

- **Responses**: sufijo `ResponseDto` (`OrderDetailsResponseDto`).
- **Requests**: sufijo `RequestDto` (`OrderCreationRequestDto`).
- Nombre refleja el **caso de uso específico**, no el tipo genérico de dato (evitar `DataDto`, `InfoDto`).
- Operaciones concretas llevan el propósito explícito en el nombre (`OrderCancellationRequestDto`, `PaymentValidationRequestDto`).
- DTOs compuestos reflejan la agrupación (`OrderWithItemsResponseDto`).
- Estructura de carpetas: `dto/request/` + `dto/response/` (preferido) por dominio; si conviven formas cortas legacy (`req/`/`resp/`), no mezclar dentro de un mismo dominio — mantener consistencia local y migrar oportunísticamente al tocar el dominio.
- Un DTO = una responsabilidad. Atributos autoexplicativos en `camelCase`. Sin sufijos redundantes (`OrderDetailsResponseDto`, no `OrderOrderDetailsResponseDto`).

## 7. Documentación de API (OpenAPI/Swagger)

- Tag a nivel de clase con el nombre de la colección de recursos en mayúsculas plural (`USERS`, `ITEMS`), mismo tag/descripción para todos los controllers de una misma carpeta de dominio.
- `@Operation` con `summary` corto y `description` explicando qué recibe/devuelve.
- Cada parámetro de path/header/query documentado con nombre, tipo de ubicación, descripción, si es requerido, y ejemplo.
- Cada propiedad de DTO (request y response) documentada con descripción y ejemplo; requests marcan explícitamente si el campo es requerido u opcional.
- Declarar **todos** los status codes posibles del endpoint, con schema de error genérico para los códigos de fallo.

## 8. Formato de fechas

- Fijar una convención única de zona horaria y formato para toda la comunicación cliente-servidor (en el proyecto de referencia: `DD/MM/YYYY HH:mm (ART)`, UTC fijo, no dependiente del timezone del servidor/cliente).
- Validar formato y zona horaria tanto al enviar como al recibir, antes de persistir.

## 9. Convenciones de base de datos

- `snake_case` para todos los identificadores; tablas en plural, columnas en singular.
- PK siempre `id`; FK siempre `<entidad>_id`.
- Timestamps de instante terminan en `_at` (`TIMESTAMP WITHOUT TIME ZONE`); fechas puras en `_date` (`DATE`).
- Valores monetarios como tipo numérico de punto flotante consistente en todo el schema; precios unitarios `_price`, valores parciales `_amount`, totales `_total`.
- Booleanos con prefijo `is_`/`has_`, `NOT NULL` con `DEFAULT` coherente.
- Texto: trim antes de guardar, minúsculas cuando aplique; `VARCHAR(n)` para textos cortos, `TEXT` para libres/largos.
- Enums/estados como `VARCHAR(n)` con `CHECK` a nivel DB restringiendo los valores permitidos.
- Estructuras complejas (listas/objetos) serializadas como texto (JSON), con sufijo semántico (`_data`, `_config`).
- `UNIQUE` explícito donde el dominio lo requiera; `NOT NULL` + `DEFAULT` combinados cuando exista un valor inicial lógico; `NULL` solo si representa un estado válido de negocio.
- Migraciones versionadas (Flyway o equivalente) como única fuente de verdad del schema — el ORM no debe generar/alterar DDL automáticamente en ningún ambiente más allá de local ad-hoc.
- Editar una migración ya escrita solo mientras siga en una rama de feature sin llegar a producción; una vez en prod, migraciones son append-only.

## 10. Testing

### Estructura

```
test/.../useCase/{domain}/
├── integration/
│   ├── repository/      # repository ↔ DB real
│   ├── controller/       # E2E: HTTP → controller → DB real
│   ├── transactional/    # tests de rollback @Transactional
│   ├── service/          # solo si hay métodos transaccionales críticos
│   └── thirdParty/       # APIs externas
└── unit/
    ├── controller/
    ├── helper/
    ├── orchestrator/
    └── service/
```

### Pirámide de testing

| Nivel | % objetivo |
|-------|-----------|
| Unitarios | 70–80% |
| Integración | 15–20% |
| E2E | 5–10% |

### Reglas de mockeo por capa (unit tests)

- Test de controller → mockear orchestrator.
- Test de orchestrator → mockear services (mockear la **implementación concreta** vía `@InjectMocks`, no la interfaz).
- Test de service → mockear helper y repository.
- Foco de los tests de orchestrator: validar los flujos de pertenencia (`belongsTo*`) y la orquestación, no la lógica de negocio (que vive en el service/helper).

### Integration tests

- Contexto Spring real (`@SpringBootTest`), instancia compartida por clase si conviene (`PER_CLASS`) para reutilizar setup.
- Carga de datos vía scripts SQL versionados/templateados, no vía inserts programáticos dispersos — un template por clase de test, con limpieza total al final de la clase (`DELETE FROM` en orden FK-safe).
- IDs de test en rangos secuenciales dedicados por tabla (ej. `100000`, `200000`...) para evitar colisiones y facilitar debugging.
- Al agregar una entidad nueva: crear su template SQL y su `DELETE` correspondiente en el script de limpieza global.
- E2E mínimo obligatorio por feature: `@SpringBootTest` + `@AutoConfigureMockMvc` + `@Transactional` (rollback automático) + carga de template SQL, llamando al endpoint real vía MockMvc y asertando tanto la respuesta HTTP como el estado final en DB. `@MockBean`/equivalente **solo** para clientes de terceros — nunca mockear servicios o repositorios internos en un E2E.

### Tests de métodos transaccionales

- Objetivo: verificar que un fallo a mitad de una operación multi-tabla revierte **todo**, no solo la parte que falló.
- No mockear el repositorio — usar la DB real, provocar la excepción en un paso posterior y verificar que el primer paso tampoco quedó persistido.
- Dependencias: la clase bajo test y sus dependencias funcionales reales inyectadas normalmente; dependencias que solo se necesitan para `verify()` como spy; el resto mockeado.
- Ubicación/naming dedicados (`integration/transactional/`, sufijo `*TransactionTest`).

### Naming

- Clase de test: `{Entidad}{Capa}Test` (`UserServiceTest`, `OrderControllerTest`).
- Método de test: `[MétodoTesteado]_[CondiciónDelTest]` (`CreateOrder_Successful`, `CreateOrder_Failed`, `SaveStaff_WhenRestaurantDoesNotExists`).

### Migración de tests legacy

Camino recomendado para pasar de "todo integration" a la pirámide correcta:
1. Extraer lógica compleja del service a un Helper inyectable y testeable en aislamiento.
2. Reescribir tests de orchestrator como unit puro, mockeando el service.
3. Reescribir tests de service como unit puro, mockeando repository + helper.
4. Crear/completar tests de repository en integración real contra DB.

## 11. Reglas generales de código

- Sin abstracciones prematuras: no crear helpers/interfaces/flags para casos hipotéticos futuros. Tres líneas repetidas son preferibles a una abstracción prematura.
- Sin validación/manejo de errores para escenarios que no pueden ocurrir dado el flujo del sistema; validar solo en los bordes (input de usuario, respuesta de API externa).
- Sin shims de compatibilidad hacia atrás ni variables/exports "por si acaso" — si algo no se usa, se borra.
- Documentar en el código solo el **por qué** cuando no es obvio (invariantes ocultas, workarounds de bugs externos, comportamiento que sorprendería a quien lo lea); nunca el qué (eso lo dice el nombre bien elegido).

## 12. Organización de documentación interna del proyecto

Patrón recomendado para que el propio repo sea auto-descriptivo:

```
docs/
├── standards/         # convenciones vigentes (REST, controllers, DTOs, testing, cron jobs...)
│   └── README.md      # índice con un link + una línea de resumen por doc
├── database/          # convenciones de schema y guías de migración
└── specs/ (o similar) # diseños de features puntuales, uno por fecha/feature
```

Cada doc de `standards/` es corto, enfocado en un tema, con ejemplos de código reales y una sección final "Ver también" enlazando los docs relacionados — evita un único documento monolítico difícil de mantener.

## 13. Uso de agentes/asistentes especializados (si se usa IA agentic en el flujo)

Definir un enrutamiento explícito de qué tipo de tarea va a qué agente/rol (arquitectura, DB, testing, code review), y un orden de fases fijo para trabajo de feature completo (diseño → schema → implementación → tests → review), con validación del output de cada fase antes de avanzar a la siguiente.
