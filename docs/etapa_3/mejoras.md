# Informe de mejoras

## Mejora de calidad implementada

**Tipo de mejora:** Separación de responsabilidades y centralización de
reglas de negocio.

**Problema identificado:**

Con la adición de la renovación de préstamos, el sistema debe aplicar
varias reglas: el préstamo debe estar activo, no debe haber sido
renovado antes, su fecha esperada de devolución no debe ser anterior al
día actual y el plazo adicional depende del tipo de solicitante. Si
estas validaciones se colocan en `App`, en `PrestamoController` o en el
repositorio JDBC, la lógica queda distribuida entre distintas capas y
resulta más difícil mantenerla y probarla.

**Antes:**

``` java
// Ejemplo ilustrativo de lógica colocada en el controlador.
// No representa necesariamente el código anterior exacto.
if (prestamo.getEstado() == EstadoPrestamo.ACTIVO) {
    // Se decide aquí si se permite la renovación.
    // También podría calcularse aquí la nueva fecha.
}
```

**Después:**

``` java
// PrestamoService centraliza las reglas de renovación.
public Prestamo renovar(int id) {
    Prestamo prestamo = prestamos.buscarPorId(id)
        .orElseThrow(() ->
            new RenovacionNoPermitidaException(
                "No existe el préstamo indicado."
            )
        );

    LocalDate hoy = LocalDate.now();

    if (prestamo.getEstado() != EstadoPrestamo.ACTIVO) {
        throw new RenovacionNoPermitidaException(
            "Solo se pueden renovar préstamos activos."
        );
    }

    if (prestamo.getRenovaciones() != 0) {
        throw new RenovacionNoPermitidaException(
            "Este préstamo ya fue renovado."
        );
    }

    if (prestamo.getFechaDevolucionEsperada().isBefore(hoy)) {
        throw new RenovacionNoPermitidaException(
            "No se puede renovar un préstamo vencido."
        );
    }

    int dias = prestamo.getSolicitante().getTipo()
            == TipoSolicitante.ESTUDIANTE ? 3 : 7;

    LocalDate nuevaFecha =
        prestamo.getFechaDevolucionEsperada().plusDays(dias);

    boolean actualizado = prestamos.renovarPrestamo(id, nuevaFecha);

    if (!actualizado) {
        throw new RenovacionNoPermitidaException(
            "No se pudo renovar el préstamo."
        );
    }

    prestamo.setFechaDevolucionEsperada(nuevaFecha);
    prestamo.setRenovaciones(1);
    return prestamo;
}
```

**Beneficio obtenido:**

La regla de renovación queda concentrada en `PrestamoService`, lo que
facilita su lectura, mantenimiento y prueba unitaria. El controlador se
limita a recibir la solicitud y comunicar el resultado, mientras que el
repositorio se encarga de la persistencia. La actualización condicional
del repositorio también permite rechazar la operación si el préstamo ya
cambió de estado o fue renovado. El plazo se calcula desde la fecha de
devolución esperada actual: tres días para estudiantes y siete para
funcionarios. Un préstamo cuya fecha esperada sea hoy no se considera
vencido; se rechaza cuando esa fecha es anterior a hoy.

## Defectos encontrados y corregidos

  ----------------------------------------------------------------------------------------------------------------------
  ID             Defecto        Cómo se        Corrección                  
                                detectó                                    
                                (prueba)                                   
  -------------- -------------- -------------- --------------------------- ---------------------------------------------
  D-01           La nueva regla Pruebas        Validar estado, contador    
                 exige limitar  unitarias      `renovaciones` y fecha en   
                 cada préstamo  propuestas:    `PrestamoService`;          
                 a una sola     renovar un     persistir la renovación     
                 renovación y   préstamo por   mediante una actualización  
                 rechazar       segunda vez;   condicional que solo        
                 préstamos      renovar uno    permita préstamos activos   
                 devueltos o    devuelto;      sin renovación previa.      
                 vencidos.      intentar                                   
                                renovar uno                                
                                cuya fecha                                 
                                esperada sea                               
                                anterior a                                 
                                hoy. Registrar                             
                                el resultado                               
                                obtenido                                   
                                únicamente                                 
                                después de                                 
                                ejecutar las                               
                                pruebas.                                   

  D-02           La consulta de Prueba de      Incorporar el modelo de     
                 préstamos      consulta con   resultado `PrestamoVencido` 
                 vencidos       un préstamo    y el método de consulta en  
                 necesita       activo cuya    `PrestamoRepository` y      
                 presentar los  fecha esperada `PrestamoRepositoryJdbc`,   
                 datos del      sea anterior a devolviendo los campos      
                 solicitante,   hoy; comprobar requeridos.                 
                 el equipo y    nombre, código                             
                 los días de    de equipo y                                
                 atraso, no     cantidad de                                
                 solamente la   días de                                    
                 entidad básica atraso.                                    
                 del préstamo.                                             
  ----------------------------------------------------------------------------------------------------------------------
  
| ID | Verificación realizada | Resultado |
|---|---|---|
| D-01 | Se verificó la regla de renovación de préstamos: solo se permite renovar una vez, únicamente cuando el préstamo está activo y su fecha de devolución esperada no ha vencido. También se comprobó que los préstamos devueltos, vencidos o previamente renovados sean rechazados. | **Completa.** Las verificaciones cumplen con los resultados esperados. |
| D-02 | Se verificó la consulta de préstamos vencidos para comprobar que identifique los préstamos activos cuya fecha de devolución esperada sea anterior a la fecha actual y que muestre el nombre del solicitante, el código del equipo y la cantidad de días de atraso. | **Completa.** Las verificaciones cumplen con los resultados esperados. |
