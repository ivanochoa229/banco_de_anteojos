# Modelo de Casos de Uso

## Sistema Banco de Anteojos — Fundación Hacer Futuro

> Complementa el SRS (`docs/srs/SRS.md`). Modela el sistema en términos de actores y casos de uso
> **esenciales** (independientes de la interfaz gráfica concreta); el diseño de interfaz de cada
> caso de uso se detalla en el entregable "Casos de Uso Reales", junto con los mockups.

---

## 1. Actores

| Actor | Tipo | Descripción |
|---|---|---|
| **Operador** | Primario, humano | Personal de la fundación: registra solicitantes, donaciones, asigna marcos, gestiona turnos y envíos. |
| **Administrador** | Primario, humano | Además de lo que hace el Operador, administra usuarios, configuración y el catálogo de venta. |
| **Beneficiario** | Primario, humano | Solicitante del banco de anteojos. En el portal de autogestión pide turno, sube el comprobante del bono contribución, consulta el estado de su marco y usa el probador virtual. |
| **Comprador** | Primario, humano | Público general que navega el catálogo de anteojos de sol (sin necesidad de cuenta). |
| **RENAPER (SID)** | Secundario, sistema externo | Valida la identidad del solicitante a partir del DNI. |
| **17TRACK** | Secundario, sistema externo | Registra y notifica (webhook) el estado de los envíos de Vía Cargo. |

## 2. Diagrama general de casos de uso

![Diagrama general](img/uc-00-general.png)

---

## 3. Módulo 1 — Solicitantes y Validación

![Módulo 1](img/uc-01-solicitantes.png)

| ID | Nombre | Actor(es) | Precondición | Flujo básico | Postcondición | RF |
|---|---|---|---|---|---|---|
| UC-01 | Registrar solicitante | Operador | — | El operador ingresa datos personales y DNI del beneficiario; el sistema valida formato y unicidad, y crea el registro. | Solicitante creado, identidad no validada. | RF-01 |
| UC-02 | Validar identidad (RENAPER) | Operador, RENAPER | Solicitante registrado (UC-01). | El sistema consulta el SID de RENAPER con el DNI; si coincide, marca la identidad como validada. | Identidad validada o rechazada. | RF-02 |
| UC-06 | Validar identidad presencial | Operador | RENAPER no disponible. | El operador verifica el DNI físico y confirma manualmente la identidad. | Identidad validada por vía alternativa, trazada como manual. | RF-06 |
| UC-03 | Cargar certificado ANSES | Operador | Solicitante registrado. | El operador adjunta el PDF de Certificación Negativa; el sistema lo sube a R2 y lo asocia al solicitante. | Certificado cargado, pendiente de verificación. | RF-03 |
| UC-04 | Verificar CUIL (código de barras) | Operador | Certificado cargado (UC-03). | El sistema decodifica el código de barras del PDF y compara/asocia el CUIL contra el solicitante. | CUIL confirmado o rechazo con motivo. | RF-04 |
| UC-05 | Verificar vigencia ANSES | Operador | Certificado cargado (UC-03). | El sistema extrae la fecha de emisión y calcula la antigüedad; marca vencido si supera 30 días. | Estado de vigencia informado (no bloqueante). | RF-05 |
| UC-07 | Registrar receta médica | Operador | Solicitante registrado. | El operador carga los valores de graduación (y opcionalmente el archivo de la receta). | Receta registrada, disponible para el match (UC-14). | RF-07 |
| UC-08 | Gestionar solicitantes (listar/editar) | Operador | — | El operador busca, consulta el detalle o edita los datos de un solicitante (DNI no editable). | Listado, detalle o edición confirmada. | RF-08 |

## 4. Módulo 2 — Donaciones

![Módulo 2](img/uc-02-donaciones.png)

| ID | Nombre | Actor(es) | Precondición | Flujo básico | Postcondición | RF |
|---|---|---|---|---|---|---|
| UC-09 | Registrar donante | Operador | — | El operador registra un donante (persona o entidad) con sus datos de contacto. | Donante creado. | RF-09 |
| UC-10 | Registrar recepción de marcos | Operador | Donante registrado. | El operador registra la cantidad y datos de los marcos recibidos; el sistema crea cada marco en estado `AVAILABLE` con su precinto único. | Marcos en inventario, vinculados al donante. | RF-10 |
| UC-11 | Clasificar marco por atributos | Operador | Marco recibido (UC-10). | El operador carga tipo, material y medidas del marco. | Marco clasificado, disponible para el match. | RF-11 |
| UC-12 | Consultar donaciones y donantes | Operador | — | El operador busca donantes y consulta el historial de marcos donados por cada uno. | Listado/detalle mostrado. | RF-12 |

## 5. Módulo 3 — Inventario, Asignación y Trazabilidad

![Módulo 3](img/uc-03-inventario.png)

| ID | Nombre | Actor(es) | Precondición | Flujo básico | Postcondición | RF |
|---|---|---|---|---|---|---|
| UC-13 | Consultar inventario | Operador | — | El operador consulta el inventario de marcos, filtrable por estado y atributos. | Vista de inventario actualizada. | RF-13 |
| UC-14 | Ejecutar match receta↔marco | Operador | Receta registrada (UC-07). | El sistema filtra/ordena los marcos disponibles compatibles con la graduación y atributos requeridos. | Lista de marcos candidatos. | RF-14 |
| UC-15 | Asignar marco a beneficiario | Operador | Match ejecutado (UC-14). | El operador elige un marco candidato; el sistema crea la asignación y descuenta el marco del inventario disponible. | Asignación creada, marco en estado `ASSIGNED`. | RF-15 |
| UC-16 | Consultar trazabilidad | Operador, Beneficiario | Asignación creada (UC-15). | Se consulta el historial de la asignación (envío a óptica, retorno, entrega) por asignación o por marco. | Historial mostrado. | RF-16 |

## 6. Módulo 4 — Probador Virtual

![Módulo 4](img/uc-04-probador.png)

| ID | Nombre | Actor(es) | Precondición | Flujo básico | Postcondición | RF |
|---|---|---|---|---|---|---|
| UC-17 | Detectar rostro | Beneficiario, Operador | Foto estática disponible (cámara o archivo). | MediaPipe Face Mesh procesa la foto en el navegador y obtiene los landmarks faciales. | Landmarks calculados, o estado "no disponible" si falla. | RF-17 |
| UC-18 | Superponer marco (2D) | Beneficiario, Operador | Rostro detectado (UC-17). | El sistema posiciona y escala el marco elegido sobre la foto usando Canvas; permite ajuste manual de rotación/altura. | Previsualización renderizada. | RF-18 |
| UC-19 | Seleccionar marco a probar | Beneficiario, Operador | Superposición activa (UC-18). | El usuario elige otro marco del inventario/catálogo disponible. | Previsualización recalculada para el nuevo marco. | RF-19 |

## 7. Módulo 5 — Turnos y Notificaciones

![Módulo 5](img/uc-05-turnos.png)

| ID | Nombre | Actor(es) | Precondición | Flujo básico | Postcondición | RF |
|---|---|---|---|---|---|---|
| UC-20 | Registrar turno | Operador, Beneficiario | Solicitante existente. | Se elige fecha/hora disponible; si lo crea el beneficiario por autogestión, el turno nace `PENDING_PAYMENT`. | Turno creado. | RF-20 |
| UC-20b | Cargar comprobante de bono contribución | Beneficiario | Turno `PENDING_PAYMENT` (UC-20). | El beneficiario sube el comprobante de la transferencia. | Turno pasa a `SCHEDULED`, dispara notificación (UC-22). | RF-20 |
| UC-21 | Reprogramar o cancelar turno | Operador, Beneficiario | Turno existente; pertenencia validada. | Se cambia la fecha/hora, o se cancela con motivo. | Turno actualizado (`SCHEDULED` con nueva fecha, o `CANCELLED`). | RF-21 |
| UC-22 | Notificar turno | Sistema (disparado por UC-20/UC-21) | Evento de turno ocurrido. | Se envía la notificación correspondiente; un fallo de envío no interrumpe el flujo principal. | Notificación entregada (best-effort). | RF-22 |

## 8. Módulo 6 — Logística e Integración 17TRACK

![Módulo 6](img/uc-06-logistica.png)

| ID | Nombre | Actor(es) | Precondición | Flujo básico | Postcondición | RF |
|---|---|---|---|---|---|---|
| UC-23 | Registrar envío (17TRACK) | Operador, 17TRACK | Marco/lote a enviar entre sucursales. | El operador carga los datos del envío; el sistema lo registra en 17TRACK vía `/register`. | Envío creado y en seguimiento. | RF-23 |
| UC-24 | Recibir actualización (webhook) | 17TRACK | Envío registrado (UC-23). | 17TRACK notifica un cambio de estado por webhook; el sistema lo valida y actualiza el envío. | Estado del envío actualizado, evento trazado. | RF-24 |
| UC-25 | Consultar estado y trazabilidad | Operador | Envío registrado. | El operador consulta el estado actual y el historial de eventos del envío. | Estado + historial mostrado. | RF-25 |

## 9. Módulo 7 — Panel de Impacto

![Módulo 7](img/uc-07-impacto.png)

| ID | Nombre | Actor(es) | Precondición | Flujo básico | Postcondición | RF |
|---|---|---|---|---|---|---|
| UC-26 | Ver panel de indicadores | Operador, Administrador | — | Se calculan agregados (entregas, beneficiarios atendidos) sobre asignaciones y turnos. | Panel mostrado. | RF-26 |
| UC-27 | Filtrar métricas por período | Operador, Administrador | Panel visible (UC-26). | Se elige un rango de fechas; el sistema recalcula los indicadores acotados a ese período. | Métricas del período mostradas. | RF-27 |

## 10. Módulo 8 — Catálogo de Venta

![Módulo 8](img/uc-08-catalogo.png)

| ID | Nombre | Actor(es) | Precondición | Flujo básico | Postcondición | RF |
|---|---|---|---|---|---|---|
| UC-28 | Consultar catálogo | Comprador, Operador | — | Se listan los productos activos del catálogo, con imagen y precio. | Catálogo mostrado. | RF-28 |
| UC-29 | Registrar venta | Operador | Stock suficiente. | El operador registra la venta de un producto; el sistema descuenta stock. | Venta registrada, stock actualizado. | RF-29 |
| UC-30 | Gestionar stock del catálogo | Administrador | — | Se carga, edita o discontinúa (baja lógica) un producto. | Catálogo actualizado. | RF-30 |

---

## 11. Notas de trazabilidad

- Correspondencia 1 a 1 con los RF del SRS, salvo UC-20/UC-20b que desagregan RF-20 para reflejar el
  flujo condicionado al bono contribución (decisión de diseño, no un requisito adicional).
- Los casos de uso de este documento son **esenciales**: describen la intención del actor y la
  respuesta del sistema sin atarse a una pantalla concreta. El detalle de interacción con la UI
  (formularios, botones, mensajes) se especifica en el entregable "Casos de Uso Reales", que
  referencia los mismos IDs (UC-01, UC-02, …) y los mockups correspondientes.
