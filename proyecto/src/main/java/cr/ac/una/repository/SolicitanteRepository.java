/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cr.ac.una.repository;

/**
 *
 * @author jafet
 */
import cr.ac.una.model.Solicitante;
import java.util.List;
import java.util.Optional;

public interface SolicitanteRepository {

    void insertar(Solicitante solicitante);

    Optional<Solicitante> buscarPorId(String identificacion);

    List<Solicitante> listar();

    void actualizar(Solicitante solicitante);

    boolean existeIdentificacion(String identificacion);
}