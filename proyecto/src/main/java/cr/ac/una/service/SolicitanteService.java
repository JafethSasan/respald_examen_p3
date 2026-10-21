/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cr.ac.una.service;

/**
 *
 * @author jafet
 */
import cr.ac.una.exception.ValidacionException;
import cr.ac.una.model.Solicitante;
import cr.ac.una.repository.SolicitanteRepository;
import java.util.List;
import java.util.Optional;

/** Reglas y operaciones de negocio de solicitantes. */
public class SolicitanteService {
    private final SolicitanteRepository repo;
    public SolicitanteService(SolicitanteRepository repo) { this.repo=repo; }
    public void registrar(Solicitante s) {
        validar(s);
        if(repo.existeIdentificacion(s.getIdentificacion())) throw new ValidacionException("Ya existe un solicitante con esa identificación.");
        repo.insertar(s);
    }
    public Optional<Solicitante> buscar(String id) { return repo.buscarPorId(Validador.requerido(id,"identificación")); }
    public List<Solicitante> listar() { return repo.listar(); }
    public void actualizar(Solicitante s) {
        validar(s);
        if(!repo.existeIdentificacion(s.getIdentificacion())) throw new ValidacionException("No existe un solicitante con esa identificación.");
        repo.actualizar(s);
    }
    private void validar(Solicitante s) {
        if(s==null) throw new ValidacionException("Debe indicar los datos del solicitante.");
        s.setIdentificacion(Validador.requerido(s.getIdentificacion(),"identificación"));
        s.setNombre(Validador.requerido(s.getNombre(),"nombre"));
        s.setCorreo(Validador.requerido(s.getCorreo(),"correo")); Validador.correo(s.getCorreo());
        if(s.getTipo()==null) throw new ValidacionException("Debe seleccionar ESTUDIANTE o FUNCIONARIO.");
    }
}

