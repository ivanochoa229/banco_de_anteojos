# Requerimientos del Sistema — Banco de Anteojos

> Consolidado de requisitos funcionales (RF) y no funcionales (RNF) del proyecto.
> IDs y agrupación por módulo según la **Matriz de Trazabilidad v3**. El texto de cada requisito
> está derivado de la Declaración de Alcance y de la descripción del sistema; para el enunciado
> textual exacto de los criterios de aceptación, la fuente de verdad es el Alcance v4.
> Total: **RF-01 a RF-30** (funcionales) · **RNF-01 a RNF-11** (no funcionales).

## Roles del sistema

- **ADMIN** — administración, configuración, gestión de usuarios y catálogo.
- **OPERADOR** — operación diaria: registro de solicitantes/donaciones, validaciones, asignación, turnos.

---

## Requisitos Funcionales

### Módulo 1 — Solicitantes y Validación (RF-01 a RF-08)

| ID | Requisito |
|---|---|
| RF-01 | El sistema permite registrar un solicitante (beneficiario) con sus datos personales y número de DNI. |
| RF-02 | El sistema integra el SID de RENAPER para validar la identidad del solicitante a partir del DNI. |
| RF-03 | El sistema permite cargar el PDF de Certificación Negativa de ANSES del solicitante. |
| RF-04 | El sistema lee el código de barras del PDF de ANSES, extrae el CUIL codificado y verifica que coincide con el solicitante registrado. |
| RF-05 | El sistema extrae la fecha de emisión del PDF de ANSES y rechaza automáticamente documentos con más de 30 días de antigüedad. La validación final queda a criterio del operador. |
| RF-06 | Ante indisponibilidad del SID de RENAPER, el sistema habilita la validación presencial del DNI como alternativa documentada. |
| RF-07 | El sistema permite registrar la receta médica (graduación) del beneficiario. |
| RF-08 | El sistema permite consultar, editar y listar solicitantes. |

### Módulo 2 — Donaciones (RF-09 a RF-12)

| ID | Requisito |
|---|---|
| RF-09 | El sistema permite registrar un donante (persona o entidad). |
| RF-10 | El sistema permite registrar la recepción de marcos donados, asignando a cada uno un identificador único (precinto). |
| RF-11 | El sistema permite clasificar los marcos donados según sus atributos (tipo, material, medidas). |
| RF-12 | El sistema permite consultar y listar donaciones y donantes. |

### Módulo 3 — Inventario, Asignación y Trazabilidad (RF-13 a RF-16)

| ID | Requisito |
|---|---|
| RF-13 | El sistema mantiene el control de inventario en tiempo real de los marcos disponibles. |
| RF-14 | El sistema registra el envío del marco y la receta a la óptica que coloca los cristales, y el retorno del lente terminado (marco + cristal) para su entrega. |
| RF-15 | El sistema permite asignar un marco a un beneficiario y descuenta la unidad del inventario. |
| RF-16 | El sistema mantiene la trazabilidad end-to-end de cada par de anteojos, desde la donación hasta la entrega. |

### Módulo 4 — Probador Virtual (RF-17 a RF-19)

| ID | Requisito |
|---|---|
| RF-17 | El sistema detecta el rostro sobre una foto estática del beneficiario usando MediaPipe Face Mesh. |
| RF-18 | El sistema superpone en 2D (Canvas) el marco seleccionado sobre la foto del beneficiario. |
| RF-19 | El sistema permite seleccionar distintos marcos para previsualizar cómo lucen sobre el rostro. |

### Módulo 5 — Turnos y Notificaciones (RF-20 a RF-22)

| ID | Requisito |
|---|---|
| RF-20 | El sistema permite registrar turnos de atención para los beneficiarios. |
| RF-21 | El sistema permite gestionar los turnos (reprogramación y cancelación). |
| RF-22 | El sistema envía notificaciones automáticas asociadas a los turnos. |

### Módulo 6 — Logística e Integración 17TRACK (RF-23 a RF-25)

| ID | Requisito |
|---|---|
| RF-23 | El sistema registra los envíos en 17TRACK (endpoint `/register`) para el seguimiento de Vía Cargo. |
| RF-24 | El sistema recibe actualizaciones automáticas del estado de los envíos mediante webhook push de 17TRACK. |
| RF-25 | El sistema permite consultar el estado y la trazabilidad de los envíos entre sucursales. |

### Módulo 7 — Panel de Impacto (RF-26 a RF-27)

| ID | Requisito |
|---|---|
| RF-26 | El sistema presenta un panel de indicadores de impacto social (entregas realizadas, beneficiarios atendidos, etc.). |
| RF-27 | El sistema permite visualizar métricas agregadas por período. |

### Módulo 8 — Catálogo de Venta (RF-28 a RF-30)

| ID | Requisito |
|---|---|
| RF-28 | El sistema muestra un catálogo digital de anteojos de sol disponibles con su precio. |
| RF-29 | El sistema permite registrar una venta de anteojos de sol con descuento automático del stock disponible. |
| RF-30 | El sistema permite gestionar el stock del catálogo: carga, actualización y baja de productos. |

---

## Requisitos No Funcionales (RNF-01 a RNF-11)

| ID | Requisito |
|---|---|
| RNF-01 | Autenticación mediante JWT stateless, con roles diferenciados ADMIN y OPERADOR. |
| RNF-02 | Tratamiento de datos personales conforme a la Ley 25.326 de Protección de Datos Personales. |
| RNF-03 | Las contraseñas se almacenan con hash BCrypt; las credenciales y secretos (JWT, R2, API keys) se cargan desde configuración externa (`.env`), nunca versionados. |
| RNF-04 | El probador virtual es compatible con dispositivos de gama baja: superposición 2D sobre foto estática, sin dependencia de servidor de procesamiento externo. |
| RNF-05 | La interfaz es operable por personal no técnico de la fundación tras una capacitación mínima (~30 min). |
| RNF-06 | El sistema se despliega en producción sobre Render (backend + PostgreSQL). |
| RNF-07 | Las imágenes se almacenan en Cloudflare R2; en la base de datos se persiste únicamente la URL, no el binario. |
| RNF-08 | Los tiempos de respuesta de las operaciones frecuentes se mantienen dentro de márgenes aceptables para uso interactivo. |
| RNF-09 | Las operaciones sensibles (validaciones, asignaciones, entregas) quedan registradas para su trazabilidad/auditoría. |
| RNF-10 | El backend sigue una arquitectura en capas con separación estricta de responsabilidades y documentación del "por qué" donde no es obvio (ver `backend/CLAUDE.md`). |
| RNF-11 | El frontend es compatible con navegadores web modernos. |

---

## Notas de trazabilidad

- **RF-14 fue reescrito (2026-07-26).** Antes decía que el sistema ejecutaba "un algoritmo de match
  entre la receta médica y los atributos de los marcos disponibles". Eso no describe el circuito real:
  la fundación recibe **marcos sin cristales**, así que el marco no tiene graduación contra la cual
  matchear. La receta se carga (PDF/imagen + graduación) y se envía junto con el marco a la óptica,
  que coloca los cristales y devuelve el lente terminado. **Pendiente:** replicar este cambio en la
  Matriz de Trazabilidad v3 y en la Declaración de Alcance v4, que son la fuente textual.

- Los criterios de aceptación asociados (CA1–CA13, con CA3 dividido en **CA3a/CA3b/CA3c** para la
  validación ANSES y CA9 ampliado para stock del catálogo) están enunciados en la Declaración de
  Alcance v4. Esa es la fuente textual exacta.
- El convenio RENAPER vía TAD **no es un requisito del sistema**: es un trámite administrativo externo,
  modelado únicamente como paquete de trabajo PT-1.2.7 en la EDT.
- Este documento es el insumo funcional para el desarrollo. Si se modifica un RF/RNF en la Matriz,
  actualizar también acá para mantener consistencia.
