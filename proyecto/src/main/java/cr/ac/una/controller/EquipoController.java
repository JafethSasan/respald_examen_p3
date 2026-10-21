/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cr.ac.una.controller;

/**
 *
 * @author jafet
 */
import cr.ac.una.model.Equipo;
import cr.ac.una.model.EstadoEquipo;
import cr.ac.una.service.EquipoService;
import java.util.List;
import java.util.Optional;

/** Fachada de casos de uso para equipos. */
public class EquipoController {
    private final EquipoService service;
    public EquipoController(EquipoService service) { this.service=service; }
    public void registrar(Equipo e) { service.registrar(e); }
    public Optional<Equipo> buscar(String codigo) { return service.buscar(codigo); }
    public List<Equipo> listar() { return service.listar(); }
    public List<Equipo> listarDisponibles() { return service.listarDisponibles(); }
    public void actualizar(Equipo e) { service.actualizar(e); }
    public void cambiarEstado(String codigo,EstadoEquipo estado) { service.cambiarEstado(codigo,estado); }
}