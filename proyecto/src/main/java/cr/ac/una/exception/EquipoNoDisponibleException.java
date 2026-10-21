/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cr.ac.una.exception;

/**
 *
 * @author jafet
 */
public class EquipoNoDisponibleException
        extends ReglaNegocioException {

    public EquipoNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}