# Diagramas de Colaboración

## Sistema Banco de Anteojos — Fundación Hacer Futuro

> Notación UML de diagramas de colaboración/comunicación: objetos como cajas, enlaces (asociaciones)
> entre los que se comunican, y mensajes numerados sobre el enlace en el orden en que ocurren
> (`1`, `2`, `2.1`, `3`, …). Son la contracara de los diagramas de secuencia de la Vista de Procesos
> del Documento de Arquitectura (`docs/arquitectura/ARQUITECTURA.md`): mismas interacciones, foco en
> **quién habla con quién** en lugar de en la línea de tiempo. Se eligieron ocho escenarios
> distintos a los de la Vista de Procesos (salvo la asignación de marco, que se repite por ser el
> caso más representativo) para no duplicar contenido y cubrir más patrones arquitectónicos.

Cada diagrama fue verificado contra el código real (orchestrators y `*ServiceHandler` del backend),
no inferido del diseño en abstracto.

---

## CD-1 — Registrar solicitante (UC-01)

![CD-1](img/cd-01-registrar-solicitante.png)

Ejemplo de **orchestrator "delgado"**: un único dominio (`applicants`) está involucrado, así que
`ApplicantUseCaseHandler` no coordina nada — se limita a delegar en `ApplicantServiceHandler`. Este
patrón se repite en la mayoría de las altas simples del sistema (donantes, marcos a mano, productos
del catálogo).

## CD-2 — Iniciar sesión (login)

![CD-2](img/cd-02-login.png)

Corrige una simplificación excesiva que había quedado en la Vista de Procesos original: el
orchestrator (`AuthUseCaseHandler`) **no llama a `JwtService` ni al repositorio** — hace un único
passthrough a `SecurityServiceHandler.login(dto)`, que puertas adentro resuelve la búsqueda del
usuario, la verificación de la contraseña y la generación del JWT.

## CD-3 — Cargar y verificar certificado ANSES (UC-03/04/05)

![CD-3](img/cd-03-anses.png)

Un único `ServiceHandler` (`applicants`) combinando sus tres colaboradores permitidos: un `Helper`
del mismo dominio (`AnsesCertificateHelper`, que decodifica el código de barras), un cliente de
terceros (`R2StorageClient`) y su propio repositorio. No hay orchestrator cross-domain porque no
hace falta: todo el caso de uso vive en `applicants`.

## CD-4 — Asignar marco a beneficiario (UC-15)

![CD-4](img/cd-04-asignar-marco.png)

El escenario que más dominios cruza del sistema. Los tres mensajes cross-domain (`2`, `3`, `4`)
salen todos de `AssignmentUseCaseHandler`; `AssignmentServiceHandler` (mensaje `5`) solo conoce su
propio repositorio. Es la contraparte en colaboración del diagrama de secuencia de la sección 4.2
del Documento de Arquitectura.

## CD-5 — Cargar comprobante de bono contribución (UC-20b)

![CD-5](img/cd-05-confirmar-turno.png)

El beneficiario sube el comprobante desde el portal de autogestión: el orchestrator de
`appointments` delega en un único colaborador, `AppointmentServiceHandler` (que sube el archivo a
R2 y deja el turno en `PENDING_REVIEW`). A diferencia de la versión anterior de este flujo, subir el
comprobante ya no confirma el turno ni dispara notificación — eso ahora es un paso aparte, a cargo
de un operador (CD-8).

## CD-6 — Actualización de envío por webhook (UC-24)

![CD-6](img/cd-06-webhook-17track.png)

Único escenario del sistema **iniciado por un actor externo** (17TRACK) en vez de por un usuario
autenticado. `TrackingWebhookHelper` es un `Helper` del propio dominio `shipments` (verifica la
firma y parsea el payload), no un cliente de terceros — la llamada saliente a 17TRACK vive en
`TrackingClient`, que no participa de este flujo entrante.

## CD-7 — Registrar venta con descuento de stock (UC-29)

![CD-7](img/cd-07-registrar-venta.png)

Otro caso de orchestrator delgado (dominio único: `catalog`). La mutación de stock es
`product.sell(quantity)`, un método de intención de negocio que lanza `InsufficientStockException`
si no alcanza el stock — nunca un `setStock(...)` directo, siguiendo la regla de "entidades sin
setters" del backend.

## CD-8 — Aprobar comprobante de turno (UC-20c)

![CD-8](img/cd-08-aprobar-turno.png)

El operador revisa el comprobante cargado en CD-5 y lo aprueba: recién acá el orchestrator de
`appointments` coordina tres colaboradores, igual que hacía la versión anterior de CD-5 antes de
separarse en dos casos de uso: `AppointmentServiceHandler` (que confirma el turno, `PENDING_REVIEW`
→ `SCHEDULED`), `ApplicantService` (para obtener el email) y `NotificationClient` (best-effort: un
fallo ahí no revierte la aprobación ya persistida).

---

## Notas de trazabilidad

- CD-1, CD-3, CD-5 y CD-7 ilustran el patrón de **orchestrator delgado / único dominio** (CD-3 y
  CD-5 hablan además con un cliente de terceros, `R2StorageClient`, sin que eso las vuelva
  cross-domain: siguen dentro de `applicants` y `appointments` respectivamente).
- CD-4 y CD-8 ilustran **composición cross-domain**, siempre concentrada en el orchestrator.
- CD-5 y CD-8 son las dos mitades de un mismo flujo, partido en dos casos de uso (UC-20b/UC-20c) a
  propósito: cargar el comprobante ya no confirma el turno por sí solo, hace falta que un operador
  lo revise y apruebe.
- CD-2 y CD-6 ilustran los dos **puntos de entrada no convencionales** del sistema: login (primer
  contacto, sin pertenencia que validar) y el webhook (sin usuario autenticado en absoluto).
- La corrección aplicada en CD-2 también se propagó al diagrama de secuencia equivalente en
  `docs/arquitectura/ARQUITECTURA.md` (sección 4.1), para que ambos documentos queden consistentes.
