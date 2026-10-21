# Pruebas documentadas
 
> Documento completado a partir de las evidencias incluidas en `Testeo_de_la_etapa_3.pdf`. Los resultados se describen según las capturas disponibles; cuando la evidencia no demuestra por completo el resultado esperado, se indica expresamente.
 
## Tipo de pruebas
 
- [x] Manuales con evidencia (capturas de consola incluidas en el PDF de testeo).
 
Según las evidencias, el proyecto se ejecutó con Maven (`exec:3.5.1:exec`) y se conectó correctamente con MySQL 8.0.32, base de datos `gamabasis_ex1p3g8`
 
## Casos de prueba
 
| ID | Tipo (éxito / error) | Descripción | Datos de entrada | Resultado esperado | Resultado obtenido | Estado |
|---|---|---|---|---|---|---|
| P-01 | Éxito | Probar la conexión con la base de datos. | Seleccionar la opción 1 del menú. | El sistema informa que la conexión se realizó correctamente y no muestra excepciones. | La consola mostró `Conexión exitosa. Motor: MySQL 8.0.32` y `Base de datos: gamabasis_ex1p3g8`. | ✅ Aprobada |
| P-02 | Éxito | Consultar los equipos disponibles. | Entrar en gestión de equipos (opción 3), listar todos (opción 3) y listar disponibles (opción 4). | Se muestran los equipos disponibles con sus datos correctos. | Con **Listar todos** se mostraron cinco equipos: `214` (Lab movil, Computadora, `DISPONIBLE`), `2628667` (Platillo volador 2, Proyector, `PRESTADO`), `EQ-01` (`DISPONIBLE`), `EQ-02` (`DISPONIBLE`) y `EQ-03` (`EN_REPARACION`). Con **Listar disponibles** se mostraron `214` y `EQ-01`, ambos `DISPONIBLE`, sin incluir el equipo `PRESTADO` que aparecía entre ellos en el listado general. La captura está recortada y no muestra el resto de la salida (por ejemplo, `EQ-02`). | ✅ Aprobada |
| P-03 | Éxito | Registrar un préstamo válido y comprobar la validación de fechas. | Solicitante `604900478`, equipo `214`; se intentó primero con fecha `2026-10-11` y después con `2026-10-21`. | Una fecha anterior a hoy debe rechazarse y una fecha válida posterior a hoy debe permitir registrar el préstamo. | Con fecha `2026-10-11`, el sistema rechazó la operación e indicó que la fecha esperada debe ser posterior a hoy (`2026-10-20`). Con fecha `2026-10-21`, informó `Préstamo registrado correctamente. Número asignado: 3`. | ✅ Aprobada |
| P-04 | Éxito | Renovar un préstamo de estudiante dentro de las condiciones permitidas. | Se registró el préstamo `4` (solicitante `111111111`, equipo `EQ-02`, fecha esperada `2026-10-21`) y luego se renovó con la opción 8. | La fecha esperada aumenta 3 días desde la fecha anterior y el contador cambia a `1`. | El sistema informó `Renovación realizada correctamente`, nueva fecha esperada `2026-10-24` y `Renovaciones utilizadas: 1 de 1`. La fecha corresponde a una extensión de 3 días. | ✅ Aprobada |
| P-05 | Éxito | Consultar préstamos vencidos. | Se seleccionó la opción 9 para listar préstamos activos vencidos, primero sin préstamos vencidos y después de agregar un préstamo activo vencido. | La consulta debe mostrar el nombre del solicitante, el código del equipo y los días de atraso de los préstamos vencidos activos. | Sin préstamos vencidos, la consola respondió `No hay préstamos activos vencidos.` Después de agregar un equipo en préstamo vencido, mostró `PRÉSTAMOS ACTIVOS VENCIDOS` y `Préstamo #2 \| Solicitante: jorge F \| Equipo: 2628667 \| Días de atraso: 3`, es decir, el nombre del solicitante, el código del equipo y los días de atraso. | ✅ Aprobada |
| P-06 | Éxito | Renovar un préstamo de funcionario dentro de las condiciones permitidas. | Préstamo número `3`. | La fecha esperada aumenta 7 días desde la fecha anterior y el contador cambia a `1`. | El sistema informó `Renovación realizada correctamente`, nueva fecha esperada `2026-10-28` y `Renovaciones utilizadas: 1 de 1`. La salida es consistente con una extensión de 7 días desde `2026-10-21`, la fecha con la que se registró el préstamo en P-03. | ✅ Aprobada |
| P-07 | Error | Intentar renovar un préstamo ya renovado. | Se intentó renovar nuevamente los préstamos `4` y `3`, ambos previamente renovados. | La renovación se rechaza; la fecha y el contador no cambian. | Para ambos préstamos, el sistema indicó que el préstamo ya fue renovado y no admite otra renovación. La evidencia confirma el rechazo de la segunda renovación; no muestra una consulta posterior para verificar explícitamente que fecha y contador permanecieron sin cambios. | ✅ Aprobada |
| P-08 | Error | Intentar renovar un préstamo devuelto. | Se registró la devolución del préstamo `3` y luego se intentó renovarlo. | La renovación se rechaza y los datos del préstamo permanecen sin cambios. | La devolución se registró y el sistema indicó que el equipo volvía a estar disponible. Al intentar renovar el préstamo `3`, respondió `Solo se pueden renovar préstamos activos.` La evidencia no incluye una consulta posterior que verifique que los datos del préstamo permanecieron sin cambios. | ✅ Aprobada |
| P-09 | Error | Intentar renovar un préstamo vencido. | Préstamo número `2`. | La renovación se rechaza y los datos permanecen sin cambios. | El sistema respondió `No se puede renovar un préstamo vencido.` La evidencia confirma el rechazo; no incluye una consulta posterior de la fecha o del contador. | ✅ Aprobada |
| P-10 | Error | Registrar un préstamo con equipo no disponible. | Solicitante `604770610`, equipo `2628667`, fecha esperada `2026-11-10`. | El sistema rechaza el registro y no crea un préstamo válido para ese equipo. | El sistema respondió `El equipo está en estado PRESTADO y no se puede prestar.` | ✅ Aprobada |
| P-11 | Error | Registrar un préstamo con un solicitante inexistente. | Identificación `43567262274`, equipo `214`, fecha esperada `2027-04-04`. | El sistema rechaza la operación con un mensaje controlado y no guarda el préstamo. | El sistema respondió `No existe el solicitante indicado.` | ✅ Aprobada |
| P-12 | Error | Renovar un préstamo con identificador inexistente. | Se intentó renovar el préstamo número `7`. | El sistema informa que el préstamo no existe o que la operación no puede realizarse; no se modifican registros. | El sistema respondió `No existe el préstamo indicado.` | ✅ Aprobada |
 
## Evidencias
 
Las evidencias de las pruebas se encuentran en el archivo `Testeo_de_la_etapa_3.pdf`:
 
- **P-01:** página 1 — compilación y ejecución con Maven, y conexión exitosa con MySQL.
- **P-02:** página 2 — listado general de equipos y estados (opción 3) y listado de equipos disponibles (opción 4, captura recortada).
- **P-03:** página 3 — rechazo de fecha anterior y registro con fecha válida.
- **P-04:** página 4 — registro del préstamo `4` y renovación con nueva fecha `2026-10-24`.
- **P-05:** página 5 — consulta sin préstamos vencidos y consulta con el préstamo `#2` vencido (3 días de atraso).
- **P-06:** página 6 — renovación con nueva fecha `2026-10-28`.
- **P-07:** página 7 — rechazo de una segunda renovación (préstamos `4` y `3`).
- **P-08:** página 8 — devolución y rechazo de renovación de préstamo no activo.
- **P-09:** página 9 — rechazo de renovación de préstamo vencido.
- **P-10:** página 10 — rechazo de préstamo por equipo prestado.
- **P-11:** página 11 — rechazo por solicitante inexistente.
- **P-12:** página 12 — rechazo por número de préstamo inexistente.
## Resumen
 
| Total de casos | Aprobadas | Fallidas | Bloqueadas / por completar |
|---:|---:|---:|---:|
| 12 | 12 | 0 | 0 |
 
