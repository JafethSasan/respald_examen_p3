/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cr.ac.una.repository;

/**
 *
 * @author jafet
 */
import cr.ac.una.config.ConexionBD;
import cr.ac.una.model.Solicitante;
import cr.ac.una.model.TipoSolicitante;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Implementación JDBC del acceso a solicitantes. */
public class SolicitanteRepositoryJdbc implements SolicitanteRepository {
    private static final String COLUMNAS = "identificacion, nombre, correo, tipo";

    private Solicitante mapear(ResultSet rs) throws SQLException {
        return new Solicitante(rs.getString("identificacion"), rs.getString("nombre"),
                rs.getString("correo"), TipoSolicitante.valueOf(rs.getString("tipo")));
    }

    @Override public void insertar(Solicitante s) {
        String sql = "INSERT INTO solicitante (identificacion,nombre,correo,tipo) VALUES (?,?,?,?)";
        try (Connection con = ConexionBD.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getIdentificacion()); ps.setString(2, s.getNombre());
            ps.setString(3, s.getCorreo()); ps.setString(4, s.getTipo().name()); ps.executeUpdate();
        } catch (SQLException e) { throw JdbcUtil.error("registrar el solicitante", e); }
    }

    @Override public Optional<Solicitante> buscarPorId(String id) {
        String sql = "SELECT " + COLUMNAS + " FROM solicitante WHERE identificacion = ?";
        try (Connection con = ConexionBD.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); }
        } catch (SQLException e) { throw JdbcUtil.error("consultar el solicitante", e); }
    }

    @Override public List<Solicitante> listar() {
        List<Solicitante> lista = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM solicitante ORDER BY nombre";
        try (Connection con = ConexionBD.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) { while (rs.next()) lista.add(mapear(rs)); }
        catch (SQLException e) { throw JdbcUtil.error("listar solicitantes", e); }
        return lista;
    }

    @Override public void actualizar(Solicitante s) {
        String sql = "UPDATE solicitante SET nombre=?, correo=?, tipo=? WHERE identificacion=?";
        try (Connection con = ConexionBD.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getNombre()); ps.setString(2, s.getCorreo());
            ps.setString(3, s.getTipo().name()); ps.setString(4, s.getIdentificacion());
            if (ps.executeUpdate() == 0) throw new IllegalArgumentException("No existe el solicitante indicado.");
        } catch (SQLException e) { throw JdbcUtil.error("actualizar el solicitante", e); }
    }

    @Override public boolean existeIdentificacion(String id) {
        String sql = "SELECT 1 FROM solicitante WHERE identificacion=?";
        try (Connection con = ConexionBD.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id); try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { throw JdbcUtil.error("verificar el solicitante", e); }
    }
}