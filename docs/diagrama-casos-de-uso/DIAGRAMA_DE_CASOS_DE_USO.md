# Diagrama de Casos de Uso

## Sistema Banco de Anteojos — Fundación Hacer Futuro

> Entregable gráfico independiente: diagrama general de casos de uso más un diagrama por módulo.
> Las especificaciones textuales de cada caso de uso (actor, precondición, flujo, postcondición)
> están en `docs/casos-de-uso/CASOS_DE_USO.md`; este documento contiene solo los diagramas UML.

## Actores

| Actor | Tipo | Descripción |
|---|---|---|
| **Operador** | Primario, humano | Personal de la fundación: registra solicitantes, donaciones, asigna marcos, gestiona turnos y envíos. |
| **Administrador** | Primario, humano | Además de lo que hace el Operador, administra usuarios, configuración y el catálogo de venta. |
| **Beneficiario** | Primario, humano | Solicitante del banco de anteojos; en el portal de autogestión pide turno, sube el comprobante del bono contribución, consulta el estado de su marco y usa el probador virtual. |
| **Comprador** | Primario, humano | Público general que navega el catálogo de anteojos de sol (sin necesidad de cuenta). |
| **RENAPER (SID)** | Secundario, sistema externo | Valida la identidad del solicitante a partir del DNI. |
| **17TRACK** | Secundario, sistema externo | Registra y notifica (webhook) el estado de los envíos de Vía Cargo. |

## Diagrama general

![Diagrama general](img/uc-00-general.png)

## Módulo 1 — Solicitantes y Validación

![Módulo 1](img/uc-01-solicitantes.png)

## Módulo 2 — Donaciones

![Módulo 2](img/uc-02-donaciones.png)

## Módulo 3 — Inventario, Asignación y Trazabilidad

![Módulo 3](img/uc-03-inventario.png)

## Módulo 4 — Probador Virtual

![Módulo 4](img/uc-04-probador.png)

## Módulo 5 — Turnos y Notificaciones

![Módulo 5](img/uc-05-turnos.png)

## Módulo 6 — Logística e Integración 17TRACK

![Módulo 6](img/uc-06-logistica.png)

## Módulo 7 — Panel de Impacto

![Módulo 7](img/uc-07-impacto.png)

## Módulo 8 — Catálogo de Venta

![Módulo 8](img/uc-08-catalogo.png)

---

Ver `docs/casos-de-uso/CASOS_DE_USO.md` para la especificación textual de cada caso de uso (ID,
actores, precondición, flujo básico, postcondición, RF relacionado).
