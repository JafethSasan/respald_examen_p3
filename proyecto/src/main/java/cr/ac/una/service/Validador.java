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
import java.util.regex.Pattern;

/** Validaciones sencillas reutilizadas por los servicios. */
final class Validador {
    private static final Pattern CORREO = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private Validador() { }
    static String requerido(String valor,String campo) {
        if(valor==null || valor.isBlank()) throw new ValidacionException("El campo '"+campo+"' es obligatorio.");
        return valor.trim();
    }
    static void correo(String valor) {
        if(valor==null || !CORREO.matcher(valor.trim()).matches()) throw new ValidacionException("El correo electrónico no tiene un formato válido.");
    }
}

