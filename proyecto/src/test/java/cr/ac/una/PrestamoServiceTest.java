/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cr.ac.una;

/**
 *
 * @author jafet
 */
import cr.ac.una.exception.EquipoNoDisponibleException;
import cr.ac.una.exception.LimitePrestamosException;
import cr.ac.una.exception.ValidacionException;
import cr.ac.una.model.*;
import cr.ac.una.repository.*;
import cr.ac.una.service.PrestamoService;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import cr.ac.una.exception.RenovacionNoPermitidaException;
import cr.ac.una.model.PrestamoVencido;



/** Pruebas unitarias del servicio sin depender de una conexión MySQL. */
class PrestamoServiceTest {
    private MemSolicitantes solicitantes;
    private MemEquipos equipos;
    private MemPrestamos prestamos;
    private PrestamoService service;

    @BeforeEach void preparar() {
        solicitantes = new MemSolicitantes(); equipos = new MemEquipos(); prestamos = new MemPrestamos();
        solicitantes.insertar(new Solicitante("111", "Ana Ejemplo", "ana@example.com", TipoSolicitante.ESTUDIANTE));
        solicitantes.insertar(new Solicitante("222", "Carlos Ejemplo", "carlos@example.com", TipoSolicitante.FUNCIONARIO));
        equipos.insertar(new Equipo("EQ-1", "Laptop", "COMPUTADORA", EstadoEquipo.DISPONIBLE));
        equipos.insertar(new Equipo("EQ-2", "Proyector", "PROYECTOR", EstadoEquipo.EN_REPARACION));
        service = new PrestamoService(prestamos, solicitantes, equipos);
    }

    @Test void registraPrestamoValido() {
        Prestamo p = service.registrar("111", "EQ-1", LocalDate.now().plusDays(3));
        assertEquals(1, p.getId()); assertEquals(EstadoPrestamo.ACTIVO, p.getEstado());
        assertEquals(EstadoEquipo.PRESTADO, equipos.buscarPorCodigo("EQ-1").orElseThrow().getEstado());
    }
    @Test void rechazaEquipoEnReparacion() {
        assertThrows(EquipoNoDisponibleException.class, () -> service.registrar("111", "EQ-2", LocalDate.now().plusDays(2)));
    }
    @Test void rechazaFechaDevolucionHoy() {
        assertThrows(ValidacionException.class, () -> service.registrar("111", "EQ-1", LocalDate.now()));
    }
    @Test void rechazaSolicitanteInexistente() {
        assertThrows(ValidacionException.class, () -> service.registrar("999", "EQ-1", LocalDate.now().plusDays(2)));
    }
    @Test void rechazaEquipoInexistente() {
        assertThrows(ValidacionException.class, () -> service.registrar("111", "EQ-99", LocalDate.now().plusDays(2)));
    }
    @Test void aplicaLimiteDeDosPrestamosAEstudiante() {
        prestamos.activos.put("111", 2);
        assertThrows(LimitePrestamosException.class, () -> service.registrar("111", "EQ-1", LocalDate.now().plusDays(2)));
    }
    @Test void funcionarioPuedeTenerHastaCuatroPrestamos() {
        prestamos.activos.put("222", 3);
        Prestamo p = service.registrar("222", "EQ-1", LocalDate.now().plusDays(2));
        assertEquals(1, p.getId());
    }
    @Test void rechazaIdentificacionVacia() {
        assertThrows(ValidacionException.class, () -> service.registrar(" ", "EQ-1", LocalDate.now().plusDays(2)));
    }
    @Test void devuelvePrestamoActivo() {
        Prestamo p = service.registrar("111", "EQ-1", LocalDate.now().plusDays(2));
        service.devolver(p.getId());
        assertEquals(EstadoEquipo.DISPONIBLE, equipos.buscarPorCodigo("EQ-1").orElseThrow().getEstado());
        assertEquals(EstadoPrestamo.DEVUELTO, prestamos.buscarPorId(p.getId()).orElseThrow().getEstado());
    }
    @Test void rechazaDevolverDosVeces() {
        Prestamo p = service.registrar("111", "EQ-1", LocalDate.now().plusDays(2));
        service.devolver(p.getId());
        assertThrows(ValidacionException.class, () -> service.devolver(p.getId()));
    }

    @Test
void estudiantePuedeRenovarUnaVezYAgregaTresDias() {
    Prestamo p = service.registrar("111", "EQ-1", LocalDate.now().plusDays(5));
    LocalDate fechaOriginal = p.getFechaDevolucionEsperada();

    Prestamo renovado = service.renovar(p.getId());

    assertEquals(fechaOriginal.plusDays(3), renovado.getFechaDevolucionEsperada());
    assertEquals(1, renovado.getRenovaciones());
}

@Test
void funcionarioAgregaSieteDias() {
    Prestamo p = service.registrar("222", "EQ-1", LocalDate.now().plusDays(5));
    LocalDate fechaOriginal = p.getFechaDevolucionEsperada();

    Prestamo renovado = service.renovar(p.getId());

    assertEquals(fechaOriginal.plusDays(7), renovado.getFechaDevolucionEsperada());
    assertEquals(1, renovado.getRenovaciones());
}

@Test
void rechazaSegundaRenovacion() {
    Prestamo p = service.registrar("111", "EQ-1", LocalDate.now().plusDays(5));
    service.renovar(p.getId());

    assertThrows(RenovacionNoPermitidaException.class,
            () -> service.renovar(p.getId()));
}

@Test
void rechazaRenovarPrestamoVencido() {
    Solicitante s = solicitantes.buscarPorId("111").orElseThrow();
    Equipo e = new Equipo("EQ-V", "Equipo vencido", "COMPUTADORA", EstadoEquipo.PRESTADO);
    Prestamo vencido = new Prestamo(90, s, e, LocalDate.now().minusDays(10),
            LocalDate.now().minusDays(1), null, EstadoPrestamo.ACTIVO, 0);
    prestamos.datos.put(90, vencido);

    assertThrows(RenovacionNoPermitidaException.class,
            () -> service.renovar(90));
}

@Test
void rechazaRenovarPrestamoDevuelto() {
    Solicitante s = solicitantes.buscarPorId("111").orElseThrow();
    Equipo e = new Equipo("EQ-D", "Equipo devuelto", "COMPUTADORA", EstadoEquipo.DISPONIBLE);
    Prestamo devuelto = new Prestamo(91, s, e, LocalDate.now().minusDays(5),
            LocalDate.now().plusDays(1), LocalDate.now(), EstadoPrestamo.DEVUELTO, 0);
    prestamos.datos.put(91, devuelto);

    assertThrows(RenovacionNoPermitidaException.class,
            () -> service.renovar(91));
}

    
    private static class MemSolicitantes implements SolicitanteRepository {
        final Map<String,Solicitante> datos = new HashMap<>();
        public void insertar(Solicitante s){datos.put(s.getIdentificacion(),s);} public Optional<Solicitante> buscarPorId(String id){return Optional.ofNullable(datos.get(id));}
        public List<Solicitante> listar(){return new ArrayList<>(datos.values());} public void actualizar(Solicitante s){datos.put(s.getIdentificacion(),s);} public boolean existeIdentificacion(String id){return datos.containsKey(id);}
    }
    private static class MemEquipos implements EquipoRepository {
        final Map<String,Equipo> datos = new HashMap<>();
        public void insertar(Equipo e){datos.put(e.getCodigo(),e);} public Optional<Equipo> buscarPorCodigo(String c){return Optional.ofNullable(datos.get(c));}
        public List<Equipo> listar(){return new ArrayList<>(datos.values());} public List<Equipo> listarDisponibles(){return datos.values().stream().filter(e->e.getEstado()==EstadoEquipo.DISPONIBLE).toList();}
        public void actualizar(Equipo e){datos.put(e.getCodigo(),e);} public void actualizarEstado(String c,EstadoEquipo s){datos.get(c).setEstado(s);} public boolean existeCodigo(String c){return datos.containsKey(c);}
    }
    private static class MemPrestamos implements PrestamoRepository {
        
        public boolean renovarPrestamo(int id, LocalDate nuevaFechaEsperada) {
    Prestamo p = datos.get(id);
    if (p == null || p.getEstado() != EstadoPrestamo.ACTIVO
            || p.getRenovaciones() != 0
            || p.getFechaDevolucionEsperada().isBefore(LocalDate.now())) {
        return false;
    }
    p.setFechaDevolucionEsperada(nuevaFechaEsperada);
    p.setRenovaciones(1);
    return true;
}

public List<PrestamoVencido> listarActivosVencidos(LocalDate hoy) {
    return datos.values().stream()
            .filter(p -> p.getEstado() == EstadoPrestamo.ACTIVO)
            .filter(p -> p.getFechaDevolucionEsperada().isBefore(hoy))
            .map(p -> new PrestamoVencido(p.getId(), p.getSolicitante().getNombre(),
                    p.getEquipo().getCodigo(),
                    java.time.temporal.ChronoUnit.DAYS.between(p.getFechaDevolucionEsperada(), hoy)))
            .toList();
}

        final Map<Integer,Prestamo> datos = new HashMap<>(); final Map<String,Integer> activos = new HashMap<>(); int secuencia=0;
        public Optional<Prestamo> buscarPorId(int id){return Optional.ofNullable(datos.get(id));} public List<Prestamo> listar(){return new ArrayList<>(datos.values());}
        public List<Prestamo> listarActivosPorSolicitante(String id){return datos.values().stream().filter(p->p.getSolicitante().getIdentificacion().equals(id)&&p.getEstado()==EstadoPrestamo.ACTIVO).toList();}
        public int contarActivosPorSolicitante(String id){return activos.getOrDefault(id,0);}
        public void registrarPrestamo(Prestamo p){p.setId(++secuencia);datos.put(p.getId(),p);activos.put(p.getSolicitante().getIdentificacion(),contarActivosPorSolicitante(p.getSolicitante().getIdentificacion())+1);p.getEquipo().setEstado(EstadoEquipo.PRESTADO);}
        public void registrarDevolucion(int id,LocalDate fecha){Prestamo p=datos.get(id);p.setEstado(EstadoPrestamo.DEVUELTO);p.setFechaDevolucionReal(fecha);p.getEquipo().setEstado(EstadoEquipo.DISPONIBLE);String s=p.getSolicitante().getIdentificacion();activos.put(s,Math.max(0,contarActivosPorSolicitante(s)-1));}
    }
}