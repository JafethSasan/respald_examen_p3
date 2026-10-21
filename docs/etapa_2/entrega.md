# Etapa 2 --- Implementación funcional y persistencia

## Cambios al diseño

En esta etapa, el diseño del sistema de préstamos de equipos
tecnológicos se orienta a una implementación en Java con persistencia en
MySQL mediante JDBC. Se mantiene la arquitectura por capas para separar
responsabilidades:

-   **model:** representa las entidades y sus estados.
-   **repository:** define las operaciones de acceso a datos y sus
    implementaciones JDBC.
-   **service:** valida los datos y aplica las reglas de negocio.
-   **controller:** coordina las solicitudes entre la aplicación de
    consola y los servicios.
-   **app / util:** contiene el punto de entrada, la interacción por
    consola y utilidades de configuración.

El dominio contempla las entidades `Solicitante`, `Equipo` y `Prestamo`.
Los solicitantes pueden ser `ESTUDIANTE` o `FUNCIONARIO`; los equipos
pueden estar `DISPONIBLE`, `PRESTADO` o `EN_REPARACION`; y los préstamos
pueden estar `ACTIVO` o `DEVUELTO`.

El modelo relacional utiliza las tablas `solicitante`, `equipo` y
`prestamo`. Cada préstamo referencia a un solicitante y a un equipo
mediante claves foráneas. El diseño contempla restricciones para evitar
identificaciones y códigos duplicados, limitar los valores de los
estados y validar las fechas.

Los cambios principales respecto al diseño inicial son la incorporación
de persistencia JDBC, la separación entre interfaces de repositorio y
sus implementaciones concretas, el manejo de excepciones y la
coordinación transaccional de las operaciones que modifican tanto un
préstamo como el estado de un equipo.

> **Nota de verificación:** antes de entregar, confirme que los nombres
> de paquetes, clases, métodos y restricciones aquí descritos coincidan
> con los archivos definitivos del proyecto. Esta documentación describe
> el diseño definido para el caso; no afirma que una operación haya sido
> probada si todavía no se ha ejecutado.

## Configuración y ejecución

1.  **Crear la base de datos:** abrir phpMyAdmin o un cliente MySQL e
    importar `database/schema.sql`. El script debe crear la base de
    datos `examen_progra3` y sus tablas.

2.  **Configurar la conexión:** crear `src/main/resources/db.properties`
    a partir de `db.properties.example` y completar los valores
    correspondientes al servidor utilizado:

    ``` properties
    db.url=jdbc:mysql://localhost:3306/examen_progra3?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Costa_Rica
    db.usuario=USUARIO_MYSQL
    db.clave=CLAVE_MYSQL
    ```

    No se deben publicar credenciales reales en GitHub. Si el archivo
    contiene secretos, debe mantenerse fuera del repositorio.

3.  **Abrir el proyecto:** abrir en NetBeans la carpeta que contiene el
    `pom.xml` y comprobar que se utilice Java 21 o una versión
    compatible con la configuración del proyecto.

4.  **Compilar:** desde la terminal situada en la carpeta del proyecto,
    ejecutar:

    ``` bash
    mvn clean compile
    ```

    Si Maven no está disponible en el `PATH`, utilizar **Clean and
    Build** en NetBeans.

5.  **Ejecutar:** desde NetBeans, ejecutar la clase principal
    `cr.ac.una.app.App`. El comando `mvn exec:java` solo funcionará si
    el `pom.xml` tiene configurado el plugin correspondiente y la clase
    principal.

6.  **Comprobar la conexión:** utilizar la opción de prueba de conexión
    del menú, si está disponible, y verificar que MySQL esté activo, que
    la base de datos exista y que las credenciales sean correctas.

## Operaciones implementadas

La siguiente tabla organiza las operaciones contempladas en el alcance
del sistema y las capas responsables. Los nombres de métodos son
descriptivos; deben ajustarse a los nombres exactos del código final.

  -----------------------------------------------------------------------------------------------------------
  Operación        Tipo            Controller                Service                Repository
  ---------------- --------------- ------------------------- ---------------------- -------------------------
  Registrar        Registro        `SolicitanteController`   `SolicitanteService`   `SolicitanteRepository` /
  solicitante                                                                       implementación JDBC

  Consultar        Consulta        `SolicitanteController`   `SolicitanteService`   `SolicitanteRepository` /
  solicitante por                                                                   implementación JDBC
  identificación                                                                    

  Listar           Consulta        `SolicitanteController`   `SolicitanteService`   `SolicitanteRepository` /
  solicitantes                                                                      implementación JDBC

  Actualizar       Actualización   `SolicitanteController`   `SolicitanteService`   `SolicitanteRepository` /
  solicitante                                                                       implementación JDBC

  Registrar equipo Registro        `EquipoController`        `EquipoService`        `EquipoRepository` /
                                                                                    implementación JDBC

  Consultar equipo Consulta        `EquipoController`        `EquipoService`        `EquipoRepository` /
  por código y                                                                      implementación JDBC
  listar equipos                                                                    

  Actualizar       Actualización   `EquipoController`        `EquipoService`        `EquipoRepository` /
  información o                                                                     implementación JDBC
  estado de equipo                                                                  

  Registrar        Negocio         `PrestamoController`      `PrestamoService`      `PrestamoRepository`,
  préstamo                                                                          `SolicitanteRepository` y
                                                                                    `EquipoRepository`

  Consultar        Consulta        `PrestamoController`      `PrestamoService`      `PrestamoRepository`
  préstamos                                                                         
  activos por                                                                       
  solicitante                                                                       

  Registrar        Negocio /       `PrestamoController`      `PrestamoService`      `PrestamoRepository` y
  devolución       actualización                                                    `EquipoRepository`
  -----------------------------------------------------------------------------------------------------------

## Reglas de negocio implementadas

Las reglas definidas para el caso son las siguientes. En la columna de
excepción se indican los tipos previstos en el diseño; verificar que
sean los nombres utilizados en el código final.

  -------------------------------------------------------------------------------------------
  ID                Regla              Clase / método del     Excepción prevista
                                       Service                
  ----------------- ------------------ ---------------------- -------------------------------
  RN-01             Solo se puede      `PrestamoService`,     `EquipoNoDisponibleException` o
                    prestar un equipo  operación de registro  `ReglaNegocioException`
                    existente cuyo     de préstamo            
                    estado sea                                
                    `DISPONIBLE`.                             

  RN-02             Un estudiante      `PrestamoService`,     `LimitePrestamosException`
                    puede mantener     validación del límite  
                    como máximo dos    de préstamos activos   
                    préstamos activos                         
                    y un funcionario,                         
                    como máximo                               
                    cuatro.                                   

  RN-03             Al devolver un     `PrestamoService`,     `ReglaNegocioException` o
                    equipo, el         operación de           excepción específica de
                    préstamo pasa a    devolución             validación
                    `DEVUELTO`, se                            
                    registra la fecha                         
                    real y el equipo                          
                    vuelve a                                  
                    `DISPONIBLE`. No                          
                    debe permitirse                           
                    devolver un                               
                    préstamo                                  
                    inexistente o ya                          
                    devuelto.                                 

  RN-04             No se permiten     `SolicitanteService` y `ValidacionException` o
                    identificaciones   `EquipoService`, antes excepción de duplicidad
                    de solicitante ni  de registrar           definida por el proyecto
                    códigos de equipo                         
                    duplicados.                               

  RN-05             La fecha esperada  `PrestamoService`,     `ValidacionException`
                    de devolución debe validación de fechas   
                    ser posterior a la                        
                    fecha del préstamo                        
                    y debe cumplir las                        
                    validaciones                              
                    temporales de la                          
                    operación.                                
  -------------------------------------------------------------------------------------------

## JDBC

El acceso a la base de datos debe realizarse mediante JDBC. Las
consultas deben utilizar `PreparedStatement` con parámetros y los
recursos deben cerrarse mediante `try-with-resources`. El siguiente
fragmento ilustra el patrón para buscar un solicitante por
identificación; debe cotejarse con el método real del repositorio antes
de presentarlo como código extraído del proyecto.

``` java
public Optional<Solicitante> buscarPorIdentificacion(String identificacion) {
    String sql = "SELECT identificacion, nombre, correo, tipo "
               + "FROM solicitante WHERE identificacion = ?";

    try (Connection con = ConexionBD.obtenerConexion();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, identificacion);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return Optional.of(mapearSolicitante(rs));
            }
            return Optional.empty();
        }
    } catch (SQLException e) {
        throw new DatosException(
            "Error al consultar el solicitante " + identificacion, e
        );
    }
}
```

### ¿Alguna operación usa transacción? ¿Cuál y por qué?

La operación principal que debe ejecutarse como una transacción es el
**registro de un préstamo**, porque modifica más de una tabla: inserta
el préstamo y cambia el estado del equipo de `DISPONIBLE` a `PRESTADO`.
Ambas modificaciones deben confirmarse juntas. Si alguna falla, se debe
ejecutar `rollback` para evitar que quede registrado un préstamo sin
actualizar el equipo, o que el equipo quede prestado sin un préstamo
asociado.

La devolución también debe mantener la consistencia entre el préstamo y
el equipo. Al registrar la devolución, se actualiza el préstamo a
`DEVUELTO`, se guarda la fecha real y se cambia el equipo a
`DISPONIBLE`. Si estas modificaciones se realizan en una sola operación
de negocio, deben coordinarse transaccionalmente.

En JDBC, una transacción manual se implementa desactivando `autoCommit`,
ejecutando las operaciones, llamando a `commit` si todas terminan
correctamente y a `rollback` ante un error. **Debe verificarse en el
código fuente que esta transacción esté efectivamente implementada antes
de declarar la operación como terminada.**

## Service

El servicio central del caso es `PrestamoService`, que coordina la
validación de solicitantes, equipos, límites de préstamos y fechas. El
servicio debe depender de las interfaces de repositorio recibidas por
constructor, en lugar de crear directamente las implementaciones JDBC.
El siguiente fragmento es ilustrativo y debe ajustarse a los métodos
exactos disponibles en el proyecto.

``` java
public class PrestamoService {
    private final SolicitanteRepository solicitantes;
    private final EquipoRepository equipos;
    private final PrestamoRepository prestamos;

    public PrestamoService(
            SolicitanteRepository solicitantes,
            EquipoRepository equipos,
            PrestamoRepository prestamos) {
        this.solicitantes = solicitantes;
        this.equipos = equipos;
        this.prestamos = prestamos;
    }

    // La operación de préstamo debe validar los datos, comprobar que existan
    // el solicitante y el equipo, verificar disponibilidad y límite de préstamos,
    // validar fechas y registrar el préstamo junto con el cambio de estado
    // del equipo dentro de una transacción.
}
```

La responsabilidad del `Service` es aplicar las reglas de negocio y
coordinar los repositorios. El SQL debe permanecer en `repository`,
mientras que la lectura de datos por consola y la presentación de
mensajes corresponden a la aplicación y los controladores.

## Excepciones

El diseño contempla excepciones para distinguir errores de validación,
incumplimientos de reglas de negocio y fallos de persistencia. Los
nombres concretos deben coincidir con las clases existentes en el
proyecto.

Ejemplo de excepción personalizada:

``` java
public class LimitePrestamosException extends RuntimeException {
    public LimitePrestamosException(String mensaje) {
        super(mensaje);
    }
}
```

Esta excepción debe lanzarse desde el servicio cuando el solicitante
alcance el límite permitido de préstamos activos. El controlador o la
aplicación de consola debe capturarla para mostrar un mensaje
comprensible y permitir que el programa continúe funcionando. Los
errores de SQL deben convertirse o propagarse como errores de
persistencia ---por ejemplo, mediante `DatosException`--- y tratarse en
la capa adecuada.

Ejemplo de captura en la capa de presentación:

``` java
try {
    // Invocar el método del controller que registra el préstamo.
} catch (LimitePrestamosException e) {
    System.out.println("No se puede registrar el préstamo: " + e.getMessage());
} catch (RuntimeException e) {
    System.out.println("No fue posible completar la operación: " + e.getMessage());
}
```

Los fragmentos anteriores explican el patrón de diseño; no deben
describirse como copias literales del código real a menos que se hayan
cotejado con los archivos fuente.

## Distribución del trabajo


  -----------------------------------------------------------------------
  Integrante                          Aportes principales
  ----------------------------------- -----------------------------------
  \[jafeth\]         \[Clases, servicios,
                                      SQL, documentación o pruebas
                                      desarrolladas\]

  \[jorge\]         \[Clases, repositorios,
                                      SQL.\]
  -----------------------------------------------------------------------
