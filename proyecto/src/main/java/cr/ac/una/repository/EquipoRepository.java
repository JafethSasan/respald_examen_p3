/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cr.ac.una.repository;

/**
 *
 * @author jafet
 */
import cr.ac.una.model.Equipo;
import cr.ac.una.model.EstadoEquipo;
import java.util.List;
import java.util.Optional;

public interface EquipoRepository {

    void insertar(Equipo equipo);

    Optional<Equipo> buscarPorCodigo(String codigo);

    List<Equipo> listar();

    List<Equipo> listarDisponibles();

    void actualizar(Equipo equipo);

    void actualizarEstado(String codigo, EstadoEquipo estado);

    boolean existeCodigo(String codigo);
}