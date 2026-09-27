# Cuentas de Acceso y Credenciales de Prueba

Plataforma Web de Gestión Integral — **Banco de Anteojos · Fundación Hacer Futuro**

---

## 1. Cuentas de Acceso (Entorno Local / Desarrollo)

| Perfil / Rol | Correo Electrónico | Contraseña | Alcance y Permisos |
| :--- | :--- | :--- | :--- |
| **Administrador (`ADMIN`)** | `admin@bancodeanteojos.org` | `AdminPassword123!` | Acceso completo: Personas, donaciones, inventario, asignaciones/óptica, turnos, logística, catálogo de sol y panel de impacto. |
| **Operador / Trabajador (`OPERATOR`)** | `operador@bancodeanteojos.org` | `OperadorPassword123!` | Personal operativo del banco: Gestión diaria de solicitantes, validaciones ANSES/RENAPER, registro de donaciones y marcos, turnos y envíos. |
| **Solicitante / Beneficiario (`APPLICANT`)** | `solicitante@bancodeanteojos.org` | `SolicitantePassword123!` | Portal de autogestión de la persona: Mis turnos, seguimiento en tiempo real de su anteojo y probador virtual interactivo. |

---

## 2. Registro de Nuevos Solicitantes

Cualquier persona puede crear una cuenta de solicitante en cualquier momento:
1. Ir a `http://localhost:5173/login` (o `5174`).
2. Hacer clic en **«Registrate acá»** (o ingresar directo a `http://localhost:5173/register`).
3. Completar Nombre, Apellido, DNI, teléfono/email y contraseña.
4. Al enviar el formulario, el sistema inicia sesión automáticamente en el portal del beneficiario.

---

## 3. Aclaración sobre los Donantes (Donadores)

* **Los donantes NO tienen usuario ni contraseña.**
* Un donante es la persona o empresa que entrega armazones a la fundación.
* El equipo del Banco de Anteojos (Administrador u Operador) es quien registra a los donantes y sus marcos desde la sección **Donantes** (`/donors`) para iniciar la trazabilidad ecosocial.
