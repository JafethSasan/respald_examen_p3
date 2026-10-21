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
import cr.ac.una.model.Equipo;
import cr.ac.una.model.EstadoEquipo;
import cr.ac.una.repository.EquipoRepository;
import java.util.List;
import java.util.Optional;

/** Reglas y operaciones de negocio de equipos. */
public class EquipoService {
    private final EquipoRepository repo;
    public EquipoService(EquipoRepository repo) { this.repo=repo; }
    public void registrar(Equipo e) {
        validar(e);
        if(repo.existeCodigo(e.getCodigo())) throw new ValidacionException("Ya existe un equipo con ese código.");
        if(e.getEstado()==EstadoEquipo.PRESTADO) throw new ValidacionException("Un equipo nuevo no puede registrarse como PRESTADO; use la operación de préstamo.");
        repo.insertar(e);
    }
    public Optional<Equipo> buscar(String codigo) { return repo.buscarPorCodigo(Validador.requerido(codigo,"código")); }
    public List<Equipo> listar() { return repo.listar(); }
    public List<Equipo> listarDisponibles() { return repo.listarDisponibles(); }
    public void actualizar(Equipo e) {
        validar(e);
        Equipo actual=repo.buscarPorCodigo(e.getCodigo()).orElseThrow(()->new ValidacionException("No existe un equipo con ese código."));
        if(e.getEstado()==EstadoEquipo.PRESTADO && actual.getEstado()!=EstadoEquipo.PRESTADO)
            throw new ValidacionException("El estado PRESTADO solo se asigna al registrar un préstamo.");
        if(actual.getEstado()==EstadoEquipo.PRESTADO && e.getEstado()!=EstadoEquipo.PRESTADO)
            throw new ValidacionException("No se puede cambiar manualmente el estado de un equipo prestado; registre su devolución.");
        repo.actualizar(e);
    }
    public void cambiarEstado(String codigo, EstadoEquipo estado) {
        codigo=Validador.requerido(codigo,"código");
        if(estado==null) throw new ValidacionException("Debe seleccionar un estado válido.");
        Equipo actual=repo.buscarPorCodigo(codigo).orElseThrow(()->new ValidacionException("No existe un equipo con ese código."));
        if(estado==EstadoEquipo.PRESTADO) throw new ValidacionException("El estado PRESTADO solo se asigna al registrar un préstamo.");
        if(actual.getEstado()==EstadoEquipo.PRESTADO) throw new ValidacionException("No se puede cambiar manualmente el estado de un equipo prestado; registre su devolución.");
        repo.actualizarEstado(codigo,estado);
    }
    private void validar(Equipo e) {
        if(e==null) throw new ValidacionException("Debe indicar los datos del equipo.");
        e.setCodigo(Validador.requerido(e.getCodigo(),"código"));
        e.setDescripcion(Validador.requerido(e.getDescripcion(),"descripción"));
        e.setTipo(Validador.requerido(e.getTipo(),"tipo de equipo"));
        if(e.getEstado()==null) e.setEstado(EstadoEquipo.DISPONIBLE);
    }
}