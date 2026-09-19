# Casos de Uso Reales — Diseño de Interfaz Gráfica

## Sistema Banco de Anteojos — Fundación Hacer Futuro

> Concreta cada caso de uso esencial del `docs/casos-de-uso/CASOS_DE_USO.md` contra una pantalla real
> de `docs/mockups/MOCKUPS.md`: describe la interacción paso a paso (campos, botones, mensajes) tal
> como aparece en el wireframe correspondiente. Un caso de uso real puede reunir varios casos de uso
> esenciales cuando comparten una sola pantalla (p. ej. UC-01 a UC-07 en el detalle del solicitante).

---

## RU-01 — Iniciar sesión

**Pantalla:** Inicio de sesión (Figura 1) · **Casos de uso:** RNF-01 · **Actor:** Todos los roles

![Login](img/00-login.png)

| | |
|---|---|
| Precondición | El usuario tiene una cuenta creada (ADMIN/OPERATOR por un administrador, o APPLICANT por autoregistro). |
| Disparador | El usuario abre la aplicación sin sesión activa. |

**Flujo principal**
1. El usuario completa el campo **Email** y el campo **Contraseña**.
2. Hace clic en **Ingresar**.
3. El sistema valida las credenciales y redirige a la pantalla de inicio correspondiente a su rol
   (panel del Operador/Administrador, o portal de autogestión del Beneficiario).

**Flujos alternativos**
- **Credenciales inválidas:** el sistema muestra el mensaje de error devuelto por la API sin
  redirigir, sin indicar si falló el email o la contraseña.
- **Beneficiario sin cuenta:** hace clic en el enlace "¿Sos beneficiario? Registrate para pedir tu
  turno" y accede al formulario de registro (RF-20, autogestión).

**Postcondición:** sesión iniciada, JWT almacenado en el cliente para las siguientes requests.

**Elementos de interfaz**

| Control | Tipo | Notas |
|---|---|---|
| Email | input texto | obligatorio, formato email |
| Contraseña | input password | obligatorio |
| Ingresar | botón primario | dispara `POST /v1/auth/login` |

---

## RU-02 — Gestionar solicitantes (listar y buscar)

**Pantalla:** Solicitantes — Listado (Figura 3) · **Caso de uso:** UC-08 · **Actor:** Operador

![Solicitantes listado](img/02-solicitantes-listado.png)

| | |
|---|---|
| Precondición | El operador inició sesión. |
| Disparador | El operador entra a "Solicitantes" desde el menú lateral. |

**Flujo principal**
1. El sistema muestra la tabla de solicitantes: Nombre, DNI, estado de Identidad (badge), estado de
   ANSES (badge), fecha de la última receta.
2. El operador escribe en **Buscar por DNI o nombre…** para filtrar, o elige un estado en el
   selector **Todos los estados**.
3. El operador hace clic en **Ver** sobre una fila para ir al detalle (RU-03).
4. El operador hace clic en **+ Nuevo solicitante** para dar de alta uno nuevo (abre el formulario
   de datos personales de RU-03, vacío).

**Flujos alternativos**
- **Sin resultados:** la tabla muestra un estado vacío con el texto de búsqueda aplicado.

**Postcondición:** el operador visualiza o navega al solicitante elegido.

**Elementos de interfaz**

| Control | Tipo | Notas |
|---|---|---|
| Buscar por DNI o nombre… | input texto | filtra la tabla en vivo |
| Todos los estados | select | filtra por estado de identidad/ANSES |
| + Nuevo solicitante | botón primario | navega al alta (RU-03) |
| Badge de Identidad | badge | verde=Validada, azul=Manual (presencial), rojo=Pendiente |
| Badge de ANSES | badge | verde=Vigente, amarillo=Vencido, rojo=Sin cargar |

---

## RU-03 — Alta, edición y validación del solicitante

**Pantalla:** Solicitantes — Detalle (Figura 4) · **Casos de uso:** UC-01, UC-02, UC-06, UC-03,
UC-04, UC-05, UC-07 · **Actor:** Operador (+ RENAPER)

![Solicitantes detalle](img/03-solicitantes-detalle.png)

| | |
|---|---|
| Precondición | Solicitante nuevo (RU-02, "+ Nuevo solicitante") o existente (RU-02, "Ver"). |
| Disparador | El operador necesita registrar o completar los datos de elegibilidad de un beneficiario. |

**Flujo principal (alta y datos personales — UC-01, UC-08)**
1. El operador completa **Nombre**, **Apellido**, **DNI**, **Fecha de nacimiento**, **Teléfono** en
   la tarjeta "Datos personales".
2. Hace clic en **Guardar cambios**. El campo **DNI** queda deshabilitado para ediciones futuras
   (es la identidad contra la que se valida RENAPER).

**Flujo — Validación de identidad (UC-02, UC-06)**
3. En la tarjeta "Validación de identidad", el operador hace clic en **Reintentar validación
   RENAPER**. El sistema consulta el SID y actualiza el badge de estado ("Validada por RENAPER").
4. **Alternativo (RENAPER no disponible, UC-06):** el operador hace clic en **Validar
   presencialmente (DNI físico)**, confirma que verificó el documento en persona; el badge pasa a
   "Manual (presencial)".

**Flujo — Certificación ANSES (UC-03, UC-04, UC-05)**
5. En la tarjeta "Certificación Negativa de ANSES", el operador hace clic en **Reemplazar PDF** (o,
   si es la primera carga, el botón equivalente de subida) y selecciona el archivo.
6. El sistema decodifica el código de barras y muestra el **CUIL detectado** con el badge
   "coincide"/"no coincide" (UC-04), y la fecha de emisión con el badge "vigente (≤30 días)" /
   "vencido" (UC-05). Ninguno de los dos bloquea el guardado: quedan a criterio del operador.

**Flujo — Receta médica (UC-07)**
7. En la tarjeta "Receta médica", el operador hace clic en **+ Nueva receta** y carga los valores de
   **Esfera**, **Cilindro** y **Eje** para OD y OI (y opcionalmente el archivo de la receta).
8. La tabla de recetas se actualiza con la nueva fila.

**Flujos alternativos**
- **DNI duplicado** al guardar datos personales: el sistema rechaza el alta con mensaje de error.
- **PDF de ANSES no legible / código de barras no encontrado:** el sistema informa el error y no
  completa el CUIL/fecha detectados; el operador puede reintentar la carga.

**Postcondición:** el solicitante queda con sus datos, estado de validación y receta actualizados,
disponible para el match de asignación (RU-06).

**Elementos de interfaz**

| Control | Tipo | Notas |
|---|---|---|
| DNI | input texto | editable solo en el alta; de sólo lectura después |
| Reintentar validación RENAPER | botón | dispara la consulta al SID |
| Validar presencialmente (DNI físico) | botón secundario | registra validación manual (UC-06) |
| Reemplazar PDF | botón secundario | sube el certificado ANSES a R2 |
| + Nueva receta | botón secundario | abre el alta de una nueva graduación |

---

## RU-04 — Registrar donante y recepción de marcos

**Pantallas:** Donantes — Listado (Figura 5) y Registrar recepción de marcos (Figura 6) ·
**Casos de uso:** UC-09, UC-10, UC-11, UC-12 · **Actor:** Operador

![Donantes listado](img/04-donantes-listado.png)
![Donantes recepción](img/05-donantes-recepcion.png)

| | |
|---|---|
| Precondición | El operador inició sesión. |
| Disparador | Ingresa una donación física a la fundación. |

**Flujo principal**
1. Desde "Donantes", el operador busca con **Buscar donante…** o hace clic en **+ Nuevo donante**
   (UC-09) para cargar **Nombre / Entidad** y **Teléfono de contacto**.
2. Entra al donante y abre "Nueva recepción" (UC-10).
3. Por cada marco recibido, hace clic en **+ Agregar marco** y completa **Precinto**, **Tipo**,
   **Material** y **Medidas** (UC-11) — el precinto es el identificador único que no se repite.
4. Hace clic en **Confirmar recepción**. El sistema crea cada marco en estado `AVAILABLE`.

**Flujos alternativos**
- **Precinto duplicado:** el sistema rechaza la fila con ese precinto y pide corregirlo antes de
  confirmar.

**Postcondición:** los marcos quedan disponibles en el inventario (RU-05), vinculados al donante.

**Elementos de interfaz**

| Control | Tipo | Notas |
|---|---|---|
| + Agregar marco | botón secundario | agrega una fila editable a la tabla de marcos recibidos |
| Confirmar recepción | botón primario | crea los `Frame` en lote |

---

## RU-05 — Consultar inventario de marcos

**Pantalla:** Inventario de marcos (Figura 7) · **Caso de uso:** UC-13 · **Actor:** Operador

![Inventario](img/06-inventario.png)

| | |
|---|---|
| Precondición | Existen marcos cargados (RU-04) o a mano. |
| Disparador | El operador necesita ver qué marcos hay disponibles. |

**Flujo principal**
1. El sistema lista los marcos con Precinto, Tipo, Material, Medidas y **Estado** (badge:
   Disponible/Asignado/En óptica/Descartado).
2. El operador filtra con **Buscar por precinto…** o el selector de estado.
3. Hace clic en **Ver** para el detalle de un marco, o en **+ Cargar marco a mano** para dar de alta
   uno sin pasar por una recepción de donación.

**Postcondición:** el operador ubica el marco que necesita para una asignación (RU-06).

---

## RU-06 — Asignar marco a beneficiario

**Pantalla:** Asignación — Match y asignar marco (Figura 8) · **Casos de uso:** UC-14, UC-15 ·
**Actor:** Operador

![Asignación](img/07-asignacion.png)

| | |
|---|---|
| Precondición | El solicitante tiene identidad validada, ANSES cargado y receta registrada (RU-03). |
| Disparador | El operador decide asignar un marco al beneficiario. |

**Flujo principal**
1. El sistema muestra la receta del beneficiario (Esfera/Cilindro/Eje por ojo).
2. El sistema ejecuta el match (UC-14) y lista los marcos candidatos ordenados por compatibilidad
   (badge Alta/Media).
3. El operador hace clic en **Asignar** sobre el marco elegido.
4. El sistema crea la asignación y descuenta el marco del inventario disponible (queda `ASSIGNED`).

**Flujos alternativos**
- **Sin marcos compatibles:** la lista aparece vacía; el operador puede volver más adelante cuando
  ingrese una donación compatible.
- **El marco elegido dejó de estar disponible** (otro operador lo asignó primero): el sistema
  rechaza la asignación con un mensaje y refresca la lista.

**Postcondición:** asignación creada, visible en la trazabilidad (RU-07).

---

## RU-07 — Consultar trazabilidad de la asignación

**Pantalla:** Trazabilidad de la asignación (Figura 9) · **Caso de uso:** UC-16 · **Actor:** Operador,
Beneficiario

![Trazabilidad](img/08-trazabilidad.png)

| | |
|---|---|
| Precondición | Existe una asignación (RU-06). |
| Disparador | Se necesita conocer en qué etapa está un par de anteojos. |

**Flujo principal**
1. El sistema muestra la línea de tiempo del evento: asignado → enviado a óptica → retornado con
   cristales → entregado, cada uno con fecha y responsable.
2. Muestra el estado actual como badge (p. ej. "Entregado").

**Postcondición:** ninguna (consulta de solo lectura).

---

## RU-08 — Usar el probador virtual

**Pantalla:** Probador virtual (Figura 10) · **Casos de uso:** UC-17, UC-18, UC-19 · **Actor:**
Beneficiario, Operador

![Probador virtual](img/09-probador-virtual.png)

| | |
|---|---|
| Precondición | El navegador tiene acceso a cámara o a una foto ya tomada. |
| Disparador | El beneficiario o el operador quiere previsualizar cómo le quedan los marcos. |

**Flujo principal**
1. El usuario hace clic en **Tomar / subir foto**.
2. MediaPipe Face Mesh detecta el rostro sobre la foto estática (UC-17).
3. El usuario elige un marco de la grilla "Marcos disponibles para probar" (UC-19).
4. El sistema superpone el marco en 2D sobre la foto (UC-18) y habilita el ajuste manual de
   **Rotación** y **Altura**.

**Flujos alternativos**
- **MediaPipe no disponible** (dispositivo o red): el sistema muestra un estado degradado
  ("probador virtual no disponible en este momento") sin romper el resto de la aplicación.
- **No se detecta rostro en la foto:** el sistema pide subir otra foto.

**Postcondición:** ninguna (previsualización, no persiste datos).

---

## RU-09 — Gestionar turnos (agenda del staff)

**Pantalla:** Turnos — Agenda del staff (Figura 11) · **Casos de uso:** UC-20, UC-21, UC-22 ·
**Actor:** Operador

![Turnos agenda](img/10-turnos-agenda.png)

| | |
|---|---|
| Precondición | El solicitante existe. |
| Disparador | El operador registra o gestiona un turno presencialmente. |

**Flujo principal**
1. El operador busca con **Buscar por beneficiario…** o hace clic en **+ Registrar turno**.
2. Para un turno existente, hace clic en **Reprogramar** (elige nueva fecha/hora, el turno sigue
   `SCHEDULED`) o **Cancelar** (pide motivo, pasa a `CANCELLED`).
3. Si el turno está `Pendiente de pago` (autogestión), el operador hace clic en **Ver comprobante**
   para revisarlo antes de confirmarlo manualmente si corresponde.

**Postcondición:** el turno queda actualizado; los cambios de estado disparan la notificación
correspondiente (UC-22).

---

## RU-10 — Autogestión del beneficiario: pedir turno y pagar el bono contribución

**Pantalla:** Autogestión — Mis turnos (Figura 12) · **Casos de uso:** UC-20, UC-20b, UC-21 ·
**Actor:** Beneficiario

![Autogestión turnos](img/11-autogestion-turnos.png)

| | |
|---|---|
| Precondición | El beneficiario tiene una cuenta (rol APPLICANT) y sesión iniciada. |
| Disparador | El beneficiario quiere sacar un turno sin ir presencialmente. |

**Flujo principal**
1. El beneficiario elige una fecha disponible (paso 1 del stepper "Elegir fecha"). El turno se crea
   en estado `PENDIENTE DE PAGO`.
2. El sistema muestra los datos de la cuenta para transferir el bono contribución (paso 2,
   "Confirmar con bono contribución").
3. El beneficiario hace clic en el recuadro de **Comprobante de transferencia**, sube el archivo y
   hace clic en **Subir comprobante**.
4. El turno pasa a `SCHEDULED` (paso 3, "Turno confirmado") y dispara la notificación de
   confirmación (UC-22).

**Flujos alternativos**
- **El beneficiario no sube comprobante:** el turno permanece `Pendiente de pago` indefinidamente
  (no bloquea el resto de la app, pero no se confirma ni notifica).
- El beneficiario puede ver su **Historial** de turnos pasados (asistidos/cancelados) más abajo en
  la misma pantalla.

**Postcondición:** turno confirmado y visible en la agenda del staff (RU-09) para revisión posterior
del comprobante por un operador.

---

## RU-11 — Gestionar envíos entre sucursales

**Pantalla:** Logística — Envíos (Figura 13) · **Casos de uso:** UC-23, UC-24, UC-25 · **Actor:**
Operador (+ 17TRACK)

![Envíos](img/12-envios.png)

| | |
|---|---|
| Precondición | Hay marcos/lotes que trasladar entre sucursales. |
| Disparador | El operador necesita despachar un envío o revisar su estado. |

**Flujo principal**
1. El operador hace clic en **+ Registrar envío**, elige origen/destino y los marcos a enviar
   (UC-23). El sistema registra el envío en 17TRACK.
2. La tabla lista los envíos con su **N° de seguimiento**, ruta, estado (badge, actualizado
   automáticamente por el webhook de 17TRACK — UC-24) y última actualización.
3. El operador hace clic en **Ver historial** para el detalle de eventos de un envío (UC-25).

**Postcondición:** el envío queda trazado punta a punta entre sucursales.

---

## RU-12 — Consultar el panel de indicadores

**Pantalla:** Panel de indicadores de impacto (Figura 14) · **Casos de uso:** UC-26, UC-27 ·
**Actor:** Administrador, Operador

![Indicadores](img/13-indicadores.png)

| | |
|---|---|
| Precondición | Hay datos históricos de asignaciones/turnos/envíos. |
| Disparador | Se necesita reportar el impacto de la fundación. |

**Flujo principal**
1. El sistema muestra los indicadores agregados (Entregas realizadas, Beneficiarios atendidos,
   Marcos donados, % de turnos con asistencia) y el gráfico de entregas por mes (UC-26).
2. El usuario cambia el selector de período (p. ej. "Último trimestre") y los indicadores se
   recalculan para ese rango (UC-27).

**Postcondición:** ninguna (consulta de solo lectura).

---

## RU-13 — Consultar el catálogo público de anteojos de sol

**Pantalla:** Catálogo público (Figura 15) · **Caso de uso:** UC-28 · **Actor:** Comprador

![Catálogo público](img/14-catalogo-publico.png)

| | |
|---|---|
| Precondición | Ninguna — acceso público. |
| Disparador | Un visitante entra al catálogo desde el sitio de la fundación. |

**Flujo principal**
1. El sistema muestra los productos activos con foto, nombre y precio.
2. El visitante interesado se comunica con la fundación para coordinar la compra (registrada por un
   operador, RU-14) — el catálogo público no procesa pagos online.

---

## RU-14 — Gestionar stock y registrar ventas del catálogo

**Pantalla:** Catálogo — Gestión de stock y ventas (Figura 16) · **Casos de uso:** UC-29, UC-30 ·
**Actor:** Administrador (gestión), Operador (venta)

![Catálogo gestión](img/15-catalogo-gestion.png)

| | |
|---|---|
| Precondición | Sesión de ADMIN u OPERATOR. |
| Disparador | Llega stock nuevo, o se concreta una venta. |

**Flujo principal**
1. El administrador hace clic en **+ Nuevo producto** o en **Editar** sobre uno existente para
   cargar nombre, precio y stock (UC-30).
2. El operador hace clic en **Registrar venta** sobre un producto con stock disponible, indica la
   cantidad; el sistema descuenta el stock (UC-29).

**Flujos alternativos**
- **Stock insuficiente:** el sistema rechaza la venta con un mensaje y no descuenta stock.
- El administrador puede discontinuar un producto (baja lógica): pasa a estado "Discontinuado" y
  deja de aparecer en el catálogo público, sin borrarse del historial de ventas.

**Postcondición:** stock y/o catálogo actualizados.

---

## Notas de trazabilidad

- Cada RU referencia explícitamente su(s) UC del Modelo de Casos de Uso y su mockup en
  `docs/mockups/`; cualquier cambio de campo o botón en el mockup debe reflejarse acá.
- Las validaciones "no bloqueantes, a criterio del operador" (vigencia ANSES, compatibilidad de
  match) son decisiones de diseño explícitas del SRS (RF-05, RF-14), no omisiones.
