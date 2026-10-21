/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cr.ac.una.controller;

/**
 *
 * @author jafet
 */
import cr.ac.una.model.Solicitante;
import cr.ac.una.service.SolicitanteService;
import java.util.List;
import java.util.Optional;

/** Fachada de casos de uso para solicitantes; no contiene SQL ni reglas. */
public class SolicitanteController {
    private final SolicitanteService service;
    public SolicitanteController(SolicitanteService service) { this.service=service; }
    public void registrar(Solicitante s) { service.registrar(s); }
    public Optional<Solicitante> buscar(String id) { return service.buscar(id); }
    public List<Solicitante> listar() { return service.listar(); }
    public void actualizar(Solicitante s) { service.actualizar(s); }
}