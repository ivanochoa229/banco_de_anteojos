# Mockups

## Sistema Banco de Anteojos — Fundación Hacer Futuro

> **Capturas reales del frontend implementado** (React + Tailwind), no wireframes de diseño: se
> generaron levantando el backend y el frontend de este mismo repositorio (rama `main`) contra una
> base de datos con datos de prueba, y navegando cada pantalla como cada rol la ve realmente. El
> catálogo cubre las pantallas de los 8 módulos del sistema más autenticación, autogestión del
> beneficiario y los tres paneles de inicio por rol.
>
> Las especificaciones de interacción de cada pantalla (campos, botones, pasos) están en el
> entregable "Casos de Uso Reales", que reutiliza estas mismas capturas anotadas con letras.

---

## Índice de pantallas

| # | Pantalla | Rol(es) | Ruta real |
|---|---|---|---|
| 1 | Inicio de sesión | Todos | `/login` |
| 2 | Registro de beneficiario (autogestión) | Beneficiario | `/register` |
| 3 | Landing institucional pública | Público | `/` |
| 4 | Inicio — panel del Operador | Operador | `/operador` |
| 5 | Inicio — panel del Administrador | Administrador | `/admin` |
| 6 | Solicitantes — Listado | Operador, Administrador | `/operador/solicitantes` |
| 7 | Solicitante — Editar datos personales | Operador, Administrador | `/operador/solicitantes/:id/edit` |
| 8 | Solicitante — Recetas | Operador, Administrador | `/operador/solicitantes/:id/prescriptions` |
| 9 | Solicitante — Certificación ANSES | Operador, Administrador | `/operador/solicitantes/:id/anses-certificate` |
| 9b | Solicitante — Certificación ANSES (vencida) | Operador, Administrador | ídem |
| 10 | Donantes — Listado | Operador, Administrador | `/operador/donantes` |
| 11 | Donante — Marcos donados | Operador, Administrador | `/operador/donantes/:id/frames` |
| 12 | Inventario de marcos | Operador, Administrador | `/operador/marcos` |
| 13 | Asignaciones — Cola | Operador, Administrador | `/operador/asignaciones` |
| 14 | Asignar un marco (nueva asignación) | Operador, Administrador | `/operador/solicitantes/:id/assignments/new` |
| 15 | Asignación — Detalle y trazabilidad | Operador, Administrador | `/operador/asignaciones/:id` |
| 16 | Turnos — Agenda del staff | Operador, Administrador | `/operador/turnos` |
| 17 | Agendar un turno (staff) | Operador, Administrador | `/operador/solicitantes/:id/appointments/new` |
| 18 | Envíos — Listado | Operador, Administrador | `/operador/envios` |
| 19 | Armar un paquete de envío | Operador, Administrador | `/operador/envios/new` |
| 20 | Envío — Detalle | Operador, Administrador | `/operador/envios/:id` |
| 21 | Panel de indicadores de impacto | Operador, Administrador | `/operador/indicadores` |
| 22 | Catálogo de venta (staff) | Operador | `/operador/catalogo` |
| 23 | Probador virtual | Operador, Beneficiario | `/operador/probador`, `/solicitante/probador` |
| 24 | Catálogo — Gestión (admin) | Administrador | `/admin/catalogo` |
| 25 | Catálogo — Nuevo producto | Administrador | `/admin/catalogo/new` |
| 26 | Catálogo — Editar producto | Administrador | `/admin/catalogo/:id/edit` |
| 27 | Autogestión — Inicio del beneficiario | Beneficiario | `/solicitante` |
| 28 | Autogestión — Mis turnos | Beneficiario | `/solicitante/mis-turnos` |
| 29 | Autogestión — Estado de mi marco | Beneficiario | `/solicitante/mi-marco` |
| 30 | Autogestión — Catálogo de sol | Beneficiario | `/solicitante/catalogo` |

> El backend expone además rutas "planas" de compatibilidad (`/applicants`, `/donors`, `/frames`,
> `/assignments`, `/appointments`, `/shipments`, `/catalog`, `/indicators`, `/try-on`) que apuntan a
> las mismas pantallas — no se documentan aparte porque no cambian la interfaz, solo el prefijo.

---

### 1. Inicio de sesión
![Login](img/00-login.png)

### 2. Registro de beneficiario (autogestión)
![Registro](img/00b-register.png)

### 3. Landing institucional pública
![Landing pública](img/00c-landing-publica.png)

### 4. Inicio — panel del Operador
![Inicio operador](img/01-operador-dashboard.png)

### 5. Inicio — panel del Administrador
![Inicio administrador](img/20-admin-dashboard.png)

### 6. Solicitantes — Listado
![Solicitantes listado](img/02-solicitantes-listado.png)

### 7. Solicitante — Editar datos personales
![Solicitante editar](img/03-solicitante-editar.png)

### 8. Solicitante — Recetas
![Solicitante recetas](img/04-solicitante-recetas.png)

### 9. Solicitante — Certificación ANSES
![Solicitante ANSES](img/05-solicitante-anses.png)

### 9b. Solicitante — Certificación ANSES (vencida)
![Solicitante ANSES vencida](img/05b-solicitante-anses-vencido.png)

### 10. Donantes — Listado
![Donantes listado](img/06-donantes-listado.png)

### 11. Donante — Marcos donados
![Donante marcos](img/07-donante-marcos.png)

### 12. Inventario de marcos
![Inventario](img/08-marcos-inventario.png)

### 13. Asignaciones — Cola
![Asignaciones cola](img/09-asignaciones-cola.png)

### 14. Asignar un marco (nueva asignación)
![Asignación nueva](img/10-asignacion-nueva.png)

### 15. Asignación — Detalle y trazabilidad
![Asignación detalle](img/11-asignacion-detalle.png)

### 16. Turnos — Agenda del staff
![Turnos agenda](img/12-turnos-agenda.png)

### 17. Agendar un turno (staff)
![Turno nuevo](img/13-turno-nuevo.png)

### 18. Envíos — Listado
![Envíos listado](img/14-envios-listado.png)

### 19. Armar un paquete de envío
![Envío nuevo](img/15-envio-nuevo.png)

### 20. Envío — Detalle
![Envío detalle](img/16-envio-detalle.png)

### 21. Panel de indicadores de impacto
![Indicadores](img/17-indicadores.png)

### 22. Catálogo de venta (staff)
![Catálogo operador](img/18-catalogo-operador.png)

### 23. Probador virtual
![Probador virtual](img/19-probador-virtual.png)

> Esta captura muestra el **estado degradado real** (RNF-04): en el entorno de prueba, el detector
> de rostros de MediaPipe no pudo inicializarse y el sistema lo informa sin romper el resto de la
> aplicación, exactamente el comportamiento descrito en RF-17 y en el SRS. El flujo exitoso (foto →
> detección de rostro → superposición del marco elegido → ajuste de tamaño/altura/rotación) está
> documentado a partir del código fuente en "Casos de Uso Reales" (RU-16).

### 24. Catálogo — Gestión (admin)
![Catálogo admin](img/21-catalogo-admin.png)

### 25. Catálogo — Nuevo producto
![Producto nuevo](img/22-producto-nuevo.png)

### 26. Catálogo — Editar producto
![Producto editar](img/23-producto-editar.png)

### 27. Autogestión — Inicio del beneficiario
![Solicitante dashboard](img/24-solicitante-dashboard.png)

### 28. Autogestión — Mis turnos
![Mis turnos](img/25-mis-turnos.png)

### 29. Autogestión — Estado de mi marco
![Mi marco](img/26-mi-marco.png)

### 30. Autogestión — Catálogo de sol
![Catálogo solicitante](img/27-catalogo-solicitante.png)

---

## Notas de trazabilidad

- Todas las capturas son del **frontend real** de este repositorio (rama `main`), no recreaciones:
  se generaron corriendo `./mvnw spring-boot:run` (backend) y `npm run dev` (frontend) contra una
  base de datos PostgreSQL local sembrada con datos de prueba, y navegando autenticado con un token
  real por rol (`ADMIN`, `OPERATOR`, `APPLICANT`).
- Algunas pantallas del listado de solicitantes NO tienen buscador ni filtro por estado en la
  implementación actual (a diferencia de una versión anterior de este documento, que sí los
  mostraba): la búsqueda es un placeholder para una mejora futura, no una funcionalidad existente.
- La validación de identidad contra RENAPER (RF-02) y la validación presencial alternativa (RF-06)
  **no tienen interfaz en el frontend actual**: el campo `identityValidated` existe en el modelo de
  datos pero no hay ningún botón ni endpoint que lo cambie de `false` a `true`. La pantalla "Editar
  solicitante" (#7) refleja esto: solo tiene el formulario de datos personales.
- Los badges de estado usan la terminología real de la UI, que no siempre coincide palabra por
  palabra con el nombre del estado interno: un turno `SCHEDULED` se muestra como **"Aceptado"**, no
  "Confirmado"; `MISSED` se muestra como **"Ausente"**; los marcos usan "Aro completo" / "Medio aro"
  / "Al aire" para `FULL_RIM` / `SEMI_RIMLESS` / `RIMLESS`, y "Fuera de circulación" para
  `DISCARDED`.
- El catálogo de venta tiene tres vistas distintas según quién lo mira: gestión completa para
  Administrador (alta/edición/baja de productos + venta), venta solamente para Operador, y consulta
  desde el portal de autogestión para el Beneficiario — las tres reutilizan el mismo componente de
  listado con permisos distintos.
