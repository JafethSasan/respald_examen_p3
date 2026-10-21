/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cr.ac.una.exception;

/**
 *
 * @author jafet
 */
public class DatosException extends RuntimeException {

    public DatosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
