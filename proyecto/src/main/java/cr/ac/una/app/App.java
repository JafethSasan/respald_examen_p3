
package cr.ac.una.app;

import cr.ac.una.config.ConexionBD;
import cr.ac.una.controller.*;
import cr.ac.una.exception.DatosException;
import cr.ac.una.model.*;
import cr.ac.una.repository.*;
import cr.ac.una.service.*;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/** Punto de entrada y menú de consola. La lógica de negocio está en Service. */
public class App {
    private final SolicitanteController solicitantes;
    private final EquipoController equipos;
    private final PrestamoController prestamos;

    private App() {
        SolicitanteRepository sr=new SolicitanteRepositoryJdbc();
        EquipoRepository er=new EquipoRepositoryJdbc();
        PrestamoRepository pr=new PrestamoRepositoryJdbc();
        solicitantes=new SolicitanteController(new SolicitanteService(sr));
        equipos=new EquipoController(new EquipoService(er));
        prestamos=new PrestamoController(new PrestamoService(pr,sr,er));
    }
    public static void main(String[] args) { new App().ejecutar(); }
    private void ejecutar() {
        int opcion;
        do {
System.out.println("1. Probar conexión con MySQL");
System.out.println("2. Gestión de solicitantes");
System.out.println("3. Gestión de equipos");
System.out.println("4. Registrar préstamo");
System.out.println("5. Registrar devolución");
System.out.println("6. Consultar préstamos activos por solicitante");
System.out.println("7. Listar todos los préstamos");
System.out.println("8. Renovar préstamo");
System.out.println("9. Listar préstamos activos vencidos");
System.out.println("0. Salir");
            opcion=Consola.leerEntero("Opción: ");
            try {
                switch(opcion) {
                    case 1 -> probarConexion(); case 2 -> menuSolicitantes(); case 3 -> menuEquipos();
                    case 4 -> registrarPrestamo(); case 5 -> devolverPrestamo();
                    case 6 -> consultarActivos(); case 7 -> listarPrestamos();
                    case 8 -> renovarPrestamo();
                    case 9 -> listarPrestamosVencidos();
                    case 0 -> System.out.println("Fin del programa.");
                    default -> System.out.println("Opción no válida.");
                }
            } catch (RuntimeException ex) {
                System.out.println("No se pudo completar la operación: " + ex.getMessage());
                if (ex instanceof DatosException && ex.getCause()!=null) System.out.println("Detalle técnico: "+ex.getCause().getMessage());
            }
            System.out.println();
        } while(opcion!=0);
    }
    private void probarConexion() {
        System.out.println("Probando conexión con la base de datos...");
        try(Connection con=ConexionBD.obtenerConexion()) {
            DatabaseMetaData meta=con.getMetaData();
            System.out.println("Conexión exitosa. Motor: "+meta.getDatabaseProductName()+" "+meta.getDatabaseProductVersion());
            System.out.println("Base de datos: "+con.getCatalog());
        } catch(SQLException ex) { throw new DatosException("No fue posible conectar con MySQL: "+ex.getMessage(),ex); }
    }
    
    private void renovarPrestamo() {
    int id = Consola.leerEntero("Número de préstamo que desea renovar: ");
    Prestamo p = prestamos.renovar(id);
    System.out.println("Renovación realizada correctamente.");
    System.out.println("Nueva fecha esperada: " + p.getFechaDevolucionEsperada());
    System.out.println("Renovaciones utilizadas: " + p.getRenovaciones() + " de 1");
}

private void listarPrestamosVencidos() {
    List<PrestamoVencido> lista = prestamos.listarActivosVencidos();
    if (lista.isEmpty()) {
        System.out.println("No hay préstamos activos vencidos.");
    } else {
        System.out.println("PRÉSTAMOS ACTIVOS VENCIDOS");
        imprimir(lista);
    }
}

    private void menuSolicitantes() {
        int op; do {
            Consola.titulo("SOLICITANTES"); System.out.println("1. Registrar\n2. Consultar por identificación\n3. Listar\n4. Actualizar\n0. Volver");
            op=Consola.leerEntero("Opción: ");
            switch(op) {
                case 1 -> solicitantes.registrar(leerSolicitante(false));
                case 2 -> solicitantes.buscar(Consola.leerTexto("Identificación: ")).ifPresentOrElse(System.out::println,()->System.out.println("No se encontró el solicitante."));
                case 3 -> imprimir(solicitantes.listar());
                case 4 -> solicitantes.actualizar(leerSolicitante(true));
                case 0 -> { }
                default -> System.out.println("Opción no válida.");
            }
        } while(op!=0);
    }
    private Solicitante leerSolicitante(boolean actualizar) {
        String id=Consola.leerTexto("Identificación"+(actualizar?" a actualizar":"")+": ");
        String nombre=Consola.leerTexto("Nombre: "); String correo=Consola.leerTexto("Correo: ");
        TipoSolicitante tipo=leerEnum(TipoSolicitante.class,"Tipo (ESTUDIANTE/FUNCIONARIO): ");
        return new Solicitante(id,nombre,correo,tipo);
    }
    private void menuEquipos() {
        int op; do {
            Consola.titulo("EQUIPOS"); System.out.println("1. Registrar\n2. Consultar por código\n3. Listar todos\n4. Listar disponibles\n5. Actualizar datos\n6. Cambiar estado\n0. Volver");
            op=Consola.leerEntero("Opción: ");
            switch(op) {
                case 1 -> equipos.registrar(leerEquipo(false));
                case 2 -> equipos.buscar(Consola.leerTexto("Código: ")).ifPresentOrElse(System.out::println,()->System.out.println("No se encontró el equipo."));
                case 3 -> imprimir(equipos.listar()); case 4 -> imprimir(equipos.listarDisponibles());
                case 5 -> equipos.actualizar(leerEquipo(true));
                case 6 -> equipos.cambiarEstado(Consola.leerTexto("Código: "),leerEnum(EstadoEquipo.class,"Estado (DISPONIBLE/PRESTADO/EN_REPARACION): "));
                case 0 -> { } default -> System.out.println("Opción no válida.");
            }
        } while(op!=0);
    }
    private Equipo leerEquipo(boolean actualizar) {
        String codigo=Consola.leerTexto("Código"+(actualizar?" a actualizar":"")+": ");
        String descripcion=Consola.leerTexto("Descripción: "); String tipo=Consola.leerTexto("Tipo de equipo: ");
        EstadoEquipo estado=leerEnum(EstadoEquipo.class,"Estado (DISPONIBLE/PRESTADO/EN_REPARACION): ");
        return new Equipo(codigo,descripcion,tipo,estado);
    }
    private void registrarPrestamo() {
        String id=Consola.leerTexto("Identificación del solicitante: "); String codigo=Consola.leerTexto("Código del equipo: ");
        LocalDate fecha=leerFecha("Fecha esperada de devolución (AAAA-MM-DD): ");
        Prestamo p=prestamos.registrar(id,codigo,fecha); System.out.println("Préstamo registrado correctamente. Número asignado: "+p.getId());
    }
    private void devolverPrestamo() {
        int id=Consola.leerEntero("Número de préstamo: "); prestamos.devolver(id); System.out.println("Devolución registrada. El equipo vuelve a estar disponible.");
    }
    private void consultarActivos() {
        String id=Consola.leerTexto("Identificación del solicitante: "); List<Prestamo> lista=prestamos.listarActivosPorSolicitante(id);
        if(lista.isEmpty()) System.out.println("El solicitante no tiene préstamos activos."); else imprimir(lista);
    }
    private void listarPrestamos() { imprimir(prestamos.listar()); }
    private LocalDate leerFecha(String mensaje) {
        while(true) { String s=Consola.leerTexto(mensaje); try { return LocalDate.parse(s); }
            catch(DateTimeParseException ex) { System.out.println("Formato inválido. Use AAAA-MM-DD, por ejemplo 2026-10-20."); } }
    }
    private <E extends Enum<E>> E leerEnum(Class<E> tipo,String mensaje) {
        while(true) { String s=Consola.leerTexto(mensaje).trim().toUpperCase().replace(' ','_');
            try { return Enum.valueOf(tipo,s); } catch(IllegalArgumentException ex) { System.out.println("Valor no válido. Valores permitidos: "+String.join(", ",java.util.Arrays.stream(tipo.getEnumConstants()).map(Enum::name).toList())); } }
    }
    private void imprimir(List<?> lista) { if(lista.isEmpty()) System.out.println("No hay registros."); else lista.forEach(System.out::println); }
}