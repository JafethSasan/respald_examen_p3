# Etapa 3 — Integración, pruebas y mejora

## Integración

El sistema de préstamo de equipos tecnológicos conserva la arquitectura por capas de las entregas anteriores: `App` presenta el menú de consola; los controladores coordinan los casos de uso; los servicios aplican las reglas de negocio; los repositorios ejecutan las operaciones de persistencia mediante JDBC; y `ConexionBD` proporciona la conexión con MySQL.

La integración de esta etapa incorpora el cambio solicitado por el docente: renovar un préstamo activo una sola vez y consultar los préstamos activos vencidos. La renovación actualiza la fecha esperada y el contador `renovaciones`; la consulta de vencidos muestra el nombre del solicitante, el código del equipo y los días de atraso.

| Opción del menú | Funcionalidad | Estado |
|---|---|---|
| 1 | Probar conexión con MySQL | funcionando |
| 2 | Gestión de solicitantes: registrar, consultar, listar y actualizar | funcionando |
| 3 | Gestión de equipos: registrar, consultar, listar, listar disponibles, actualizar y cambiar estado | funcionando |
| 4 | Registrar préstamo, validando solicitante, equipo, fecha y límite de préstamos activos | funcionando |
| 5 | Registrar devolución y actualizar el estado del préstamo y del equipo | funcionando |
| 6 | Consultar préstamos activos de un solicitante | funcionando |
| 7 | Listar todos los préstamos | funcionando |
| 8 | Renovar un préstamo activo que no haya vencido ni sido renovado anteriormente | funcionando |
| 9 | Listar préstamos activos vencidos con nombre del solicitante, código del equipo y días de atraso | funcionando |
| 0 | Salir del sistema | funcionando |

> Los estados que requieren confirmación deben actualizarse después de ejecutar la versión final del proyecto. La existencia de código no sustituye la comprobación de las operaciones contra la base de datos real.

## Cambio de requerimiento

**Cambio solicitado por el docente:**

Se solicitó implementar la renovación de préstamos. Un solicitante puede renovar un préstamo activo una sola vez. Para ello se agrega el atributo entero `renovaciones`, con valor inicial `0`, y su columna correspondiente en la tabla `prestamo`. Solo se puede renovar un préstamo con estado `ACTIVO`, con `renovaciones = 0` y cuya fecha de devolución esperada no sea anterior a la fecha actual. La renovación extiende la fecha esperada vigente en tres días para estudiantes y siete días para funcionarios. También se requiere una consulta de préstamos activos vencidos que muestre el nombre del solicitante, el código del equipo y los días de atraso. Se contempla la excepción `RenovacionNoPermitidaException` para rechazar operaciones que incumplan las reglas.

**Clases modificadas o agregadas:**

| Clase | Capa | Cambio realizado |
|---|---|---|
| `Prestamo` | Modelo | Agrega el atributo `renovaciones`, inicializado en `0`, con sus métodos de acceso. |
| `PrestamoVencido` | Modelo / DTO de consulta | Nueva clase para transportar el identificador del préstamo, nombre del solicitante, código del equipo y días de atraso. |
| `RenovacionNoPermitidaException` | Excepciones | Nueva excepción específica para los rechazos de renovación por reglas de negocio. |
| `PrestamoRepository` | Repositorio | Declara las operaciones para renovar un préstamo y consultar los préstamos activos vencidos. |
| `PrestamoRepositoryJdbc` | Persistencia JDBC | Actualiza el mapeo y las consultas para incluir `renovaciones`; incorpora la actualización condicional de la renovación y la consulta de vencidos mediante las tablas relacionadas. |
| `PrestamoService` | Servicio | Valida que el préstamo exista, esté activo, no esté vencido y no se haya renovado; calcula la extensión según el tipo de solicitante y coordina la consulta de vencidos. |
| `PrestamoController` | Controlador | Expone las operaciones de renovación y listado de préstamos vencidos a la aplicación. |
| `App` | Aplicación / menú | Agrega las opciones para renovar un préstamo y listar los préstamos activos vencidos. |
| `PrestamoServiceTest` | Pruebas | Incorpora casos para comprobar renovaciones exitosas y rechazos por reglas de negocio. |
| Tabla `prestamo` | Base de datos | Agrega la columna `renovaciones INT NOT NULL DEFAULT 0`. |

**Análisis:**

La arquitectura por capas facilitó el cambio porque cada responsabilidad está separada. La regla de renovación se implementa en `PrestamoService`, mientras que `PrestamoRepositoryJdbc` se encarga de ejecutar y persistir las operaciones en MySQL. `PrestamoController` permite exponer los nuevos casos de uso sin colocar reglas de negocio directamente en el menú. La clase `PrestamoVencido` separa los datos de la consulta del modelo completo de préstamo.

Fue necesario extender el modelo `Prestamo`, actualizar el esquema de la base de datos y adaptar las consultas JDBC que recuperan o actualizan préstamos. También se deben actualizar la interfaz del repositorio, el servicio, el controlador, el menú y las pruebas unitarias para que la nueva funcionalidad esté disponible de extremo a extremo. La fecha renovada se calcula a partir de la fecha esperada vigente, no desde la fecha actual. Un préstamo cuya fecha esperada sea anterior a hoy se considera vencido; si vence hoy, no se considera vencido según la regla del docente.

## Decisiones de diseño relevantes

- **Conservar la arquitectura por capas:** las reglas de negocio permanecen en `Service`, el acceso SQL en `Repository` y la interacción con el usuario en `App` y los controladores. Esto reduce el acoplamiento y facilita probar las reglas sin una conexión real a MySQL.
- **Usar una excepción específica:** `RenovacionNoPermitidaException` distingue los rechazos de renovación de otros errores, como fallos de conexión o datos inválidos.
- **Registrar el número de renovaciones:** el atributo `renovaciones` permite hacer cumplir la regla de una sola renovación y conserva el dato en la base de datos. El valor inicial es `0` para los préstamos nuevos y existentes al agregar la columna con valor predeterminado.
- **Calcular la extensión desde la fecha esperada vigente:** se agregan tres días para estudiantes y siete para funcionarios a la fecha esperada que ya tiene el préstamo.
- **Actualizar fecha y contador en una sola operación de persistencia:** la actualización debe ser condicional al estado `ACTIVO` y a que `renovaciones = 0`, evitando que solicitudes simultáneas renueven el mismo préstamo más de una vez.
- **Separar los resultados de la consulta de vencidos:** `PrestamoVencido` representa los campos específicos requeridos por la consulta, sin devolver datos innecesarios del préstamo completo.
- **Calcular los días de atraso en la consulta:** se filtran préstamos activos cuya fecha esperada sea anterior a hoy y se obtiene la diferencia entre hoy y la fecha esperada.
- **Mantener consultas parametrizadas y recursos JDBC administrados:** se conserva el uso de `PreparedStatement` y `try-with-resources` en las operaciones de persistencia.

## Pruebas y mejoras

- Pruebas: [`pruebas.md`](pruebas.md)
- Mejoras y defectos corregidos: [`mejoras.md`](mejoras.md)

Las pruebas que deben incluirse para el cambio de requerimiento comprenden, como mínimo, una renovación exitosa y una renovación rechazada. También se deben verificar la extensión de tres días para estudiantes, la extensión de siete días para funcionarios, el rechazo de una segunda renovación, el rechazo de préstamos devueltos y vencidos, y la consulta de préstamos activos vencidos con el cálculo correcto de días de atraso.

## UML final

La versión final y completa del uml se encuentra en la carpeta img de la etapa 3

## Aspectos pendientes

No se declaran nuevas funcionalidades pendientes dentro del alcance solicitado. Antes de cerrar la entrega, se debe confirmar en la versión final que la columna `renovaciones` está presente en MySQL, que las opciones de renovación y consulta de vencidos aparecen en el menú, y que las pruebas unitarias y de integración se ejecutan correctamente. Estos puntos son verificaciones de cierre, no funcionalidades adicionales al requerimiento.



## Distribución del trabajo

| Integrante | Aportes principales |
|---|---|
| Jafeth | Trabajo en la lógica de solicitantes y equipos, revisión del modelo y validaciones, integración de los cambios en la estructura del proyecto y apoyo en documentación y pruebas. |
| Jorge | Trabajo en las operaciones de préstamos y devoluciones, persistencia JDBC y consultas, integración de la renovación y consulta de vencidos, y apoyo en documentación y pruebas. |
