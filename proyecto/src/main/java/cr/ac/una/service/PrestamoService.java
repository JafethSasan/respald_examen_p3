/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cr.ac.una.service;

/**
 *
 * @author jafet
 */
import cr.ac.una.exception.EquipoNoDisponibleException;
import cr.ac.una.exception.LimitePrestamosException;
import cr.ac.una.exception.RenovacionNoPermitidaException;
import cr.ac.una.exception.ValidacionException;
import cr.ac.una.model.*;
import cr.ac.una.repository.EquipoRepository;
import cr.ac.una.repository.PrestamoRepository;
import cr.ac.una.repository.SolicitanteRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Casos de uso y reglas de negocio para préstamos. */
public class PrestamoService {
    private final PrestamoRepository prestamos;
    private final SolicitanteRepository solicitantes;
    private final EquipoRepository equipos;

    public PrestamoService(PrestamoRepository prestamos,
                           SolicitanteRepository solicitantes,
                           EquipoRepository equipos) {
        this.prestamos = prestamos;
        this.solicitantes = solicitantes;
        this.equipos = equipos;
    }

    public Prestamo registrar(String identificacion, String codigoEquipo, LocalDate fechaEsperada) {
        identificacion = Validador.requerido(identificacion, "identificación del solicitante");
        codigoEquipo = Validador.requerido(codigoEquipo, "código del equipo");
        if (fechaEsperada == null) throw new ValidacionException("Debe indicar la fecha de devolución esperada.");
        LocalDate hoy = LocalDate.now();
        if (!fechaEsperada.isAfter(hoy)) throw new ValidacionException("La fecha esperada de devolución debe ser posterior a hoy (" + hoy + ").");
        Solicitante s = solicitantes.buscarPorId(identificacion)
                .orElseThrow(() -> new ValidacionException("No existe el solicitante indicado."));
        Equipo e = equipos.buscarPorCodigo(codigoEquipo)
                .orElseThrow(() -> new ValidacionException("No existe el equipo indicado."));
        if (e.getEstado() != EstadoEquipo.DISPONIBLE)
            throw new EquipoNoDisponibleException("El equipo está en estado " + e.getEstado() + " y no se puede prestar.");
        int limite = s.getTipo() == TipoSolicitante.ESTUDIANTE ? 2 : 4;
        int activos = prestamos.contarActivosPorSolicitante(identificacion);
        if (activos >= limite) throw new LimitePrestamosException("El solicitante ya alcanzó el límite de " + limite + " préstamos activos.");
        Prestamo p = new Prestamo(0, s, e, hoy, fechaEsperada, null, EstadoPrestamo.ACTIVO);
        prestamos.registrarPrestamo(p);
        return p;
    }

    public void devolver(int id) {
        if (id <= 0) throw new ValidacionException("El número de préstamo debe ser mayor que cero.");
        Prestamo p = prestamos.buscarPorId(id).orElseThrow(() -> new ValidacionException("No existe el préstamo indicado."));
        if (p.getEstado() != EstadoPrestamo.ACTIVO) throw new ValidacionException("El préstamo ya fue devuelto.");
        prestamos.registrarDevolucion(id, LocalDate.now());
    }

    public Prestamo renovar(int id) {
        if (id <= 0) throw new RenovacionNoPermitidaException("El número de préstamo debe ser mayor que cero.");
        Prestamo p = prestamos.buscarPorId(id)
                .orElseThrow(() -> new RenovacionNoPermitidaException("No existe el préstamo indicado."));
        LocalDate hoy = LocalDate.now();
        if (p.getEstado() != EstadoPrestamo.ACTIVO)
            throw new RenovacionNoPermitidaException("Solo se pueden renovar préstamos activos.");
        if (p.getRenovaciones() != 0)
            throw new RenovacionNoPermitidaException("Este préstamo ya fue renovado y no admite otra renovación.");
        if (p.getFechaDevolucionEsperada().isBefore(hoy))
            throw new RenovacionNoPermitidaException("No se puede renovar un préstamo vencido.");

        int dias = p.getSolicitante().getTipo() == TipoSolicitante.ESTUDIANTE ? 3 : 7;
        LocalDate nuevaFecha = p.getFechaDevolucionEsperada().plusDays(dias);
        boolean actualizado = prestamos.renovarPrestamo(id, nuevaFecha);
        if (!actualizado) {
            throw new RenovacionNoPermitidaException(
                    "No se pudo renovar: el préstamo cambió de estado, venció o ya fue renovado.");
        }
        p.setFechaDevolucionEsperada(nuevaFecha);
        p.setRenovaciones(1);
        return p;
    }

    public Optional<Prestamo> buscar(int id) {
        if (id <= 0) throw new ValidacionException("El número de préstamo debe ser mayor que cero.");
        return prestamos.buscarPorId(id);
    }

    public List<Prestamo> listar() { return prestamos.listar(); }

    public List<Prestamo> listarActivosPorSolicitante(String id) {
        id = Validador.requerido(id, "identificación");
        if (solicitantes.buscarPorId(id).isEmpty()) throw new ValidacionException("No existe el solicitante indicado.");
        return prestamos.listarActivosPorSolicitante(id);
    }

    public List<PrestamoVencido> listarActivosVencidos() {
        return prestamos.listarActivosVencidos(LocalDate.now());
    }
}

