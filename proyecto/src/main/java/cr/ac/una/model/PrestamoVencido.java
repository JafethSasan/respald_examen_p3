/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cr.ac.una.model;

/**
 *
 * @author jafet
 */
public class PrestamoVencido {
    private final int idPrestamo;
    private final String nombreSolicitante;
    private final String codigoEquipo;
    private final long diasAtraso;

    public PrestamoVencido(int idPrestamo, String nombreSolicitante,
                          String codigoEquipo, long diasAtraso) {
        this.idPrestamo = idPrestamo;
        this.nombreSolicitante = nombreSolicitante;
        this.codigoEquipo = codigoEquipo;
        this.diasAtraso = diasAtraso;
    }

    public int getIdPrestamo() { return idPrestamo; }
    public String getNombreSolicitante() { return nombreSolicitante; }
    public String getCodigoEquipo() { return codigoEquipo; }
    public long getDiasAtraso() { return diasAtraso; }

    @Override
    public String toString() {
        return "Préstamo #" + idPrestamo
                + " | Solicitante: " + nombreSolicitante
                + " | Equipo: " + codigoEquipo
                + " | Días de atraso: " + diasAtraso;
    }
}