/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cr.ac.una.repository;

/**
 *
 * @author jafet
 */
import cr.ac.una.model.Prestamo;
import cr.ac.una.model.PrestamoVencido;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PrestamoRepository {
    Optional<Prestamo> buscarPorId(int id);
    List<Prestamo> listar();
    List<Prestamo> listarActivosPorSolicitante(String identificacion);
    int contarActivosPorSolicitante(String identificacion);
    void registrarPrestamo(Prestamo prestamo);
    void registrarDevolucion(int idPrestamo, LocalDate fechaDevolucionReal);
    boolean renovarPrestamo(int idPrestamo, LocalDate nuevaFechaEsperada);
    List<PrestamoVencido> listarActivosVencidos(LocalDate hoy);
}

