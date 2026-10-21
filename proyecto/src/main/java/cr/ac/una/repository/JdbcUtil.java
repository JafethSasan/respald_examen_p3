/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cr.ac.una.repository;

/**
 *
 * @author jafet
 */
import cr.ac.una.exception.DatosException;
import java.sql.SQLException;

/** Utilidades compartidas por los repositorios JDBC. */
final class JdbcUtil {
    private JdbcUtil() { }
    static DatosException error(String operacion, SQLException e) {
        if (e.getErrorCode() == 1062) {
            return new DatosException("Ya existe un registro con una clave o correo duplicado.", e);
        }
        return new DatosException("Error al " + operacion + ": " + e.getMessage(), e);
    }
}