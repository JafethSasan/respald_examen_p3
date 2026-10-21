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
import cr.ac.una.exception.DatosException;
import cr.ac.una.model.Equipo;
import cr.ac.una.model.EstadoEquipo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Implementación JDBC del acceso a equipos. */
public class EquipoRepositoryJdbc implements EquipoRepository {
    private static final String COLUMNAS = "codigo, descripcion, tipo, estado";
    private Equipo mapear(ResultSet rs) throws SQLException {
        return new Equipo(rs.getString("codigo"), rs.getString("descripcion"), rs.getString("tipo"),
                EstadoEquipo.valueOf(rs.getString("estado")));
    }
    @Override public void insertar(Equipo e) {
        String sql = "INSERT INTO equipo (codigo,descripcion,tipo,estado) VALUES (?,?,?,?)";
        try (Connection con=ConexionBD.obtenerConexion(); PreparedStatement ps=con.prepareStatement(sql)) {
            ps.setString(1,e.getCodigo()); ps.setString(2,e.getDescripcion()); ps.setString(3,e.getTipo());
            ps.setString(4,e.getEstado().name()); ps.executeUpdate();
        } catch(SQLException ex) { throw JdbcUtil.error("registrar el equipo",ex); }
    }
    @Override public Optional<Equipo> buscarPorCodigo(String codigo) {
        String sql="SELECT "+COLUMNAS+" FROM equipo WHERE codigo=?";
        try(Connection con=ConexionBD.obtenerConexion(); PreparedStatement ps=con.prepareStatement(sql)) {
            ps.setString(1,codigo); try(ResultSet rs=ps.executeQuery()) { return rs.next()?Optional.of(mapear(rs)):Optional.empty(); }
        } catch(SQLException ex) { throw JdbcUtil.error("consultar el equipo",ex); }
    }
    @Override public List<Equipo> listar() { return listarConConsulta("SELECT "+COLUMNAS+" FROM equipo ORDER BY codigo"); }
    @Override public List<Equipo> listarDisponibles() { return listarConConsulta("SELECT "+COLUMNAS+" FROM equipo WHERE estado='DISPONIBLE' ORDER BY codigo"); }
    private List<Equipo> listarConConsulta(String sql) {
        List<Equipo> lista=new ArrayList<>();
        try(Connection con=ConexionBD.obtenerConexion(); PreparedStatement ps=con.prepareStatement(sql); ResultSet rs=ps.executeQuery()) {
            while(rs.next()) lista.add(mapear(rs));
        } catch(SQLException ex) { throw JdbcUtil.error("listar equipos",ex); }
        return lista;
    }
    @Override public void actualizar(Equipo e) {
        String sql="UPDATE equipo SET descripcion=?, tipo=?, estado=? WHERE codigo=?";
        try(Connection con=ConexionBD.obtenerConexion(); PreparedStatement ps=con.prepareStatement(sql)) {
            ps.setString(1,e.getDescripcion()); ps.setString(2,e.getTipo()); ps.setString(3,e.getEstado().name()); ps.setString(4,e.getCodigo());
            if(ps.executeUpdate()==0) throw new IllegalArgumentException("No existe el equipo indicado.");
        } catch(SQLException ex) { throw JdbcUtil.error("actualizar el equipo",ex); }
    }
    @Override public void actualizarEstado(String codigo, EstadoEquipo estado) {
        String sql="UPDATE equipo SET estado=? WHERE codigo=?";
        try(Connection con=ConexionBD.obtenerConexion(); PreparedStatement ps=con.prepareStatement(sql)) {
            ps.setString(1,estado.name()); ps.setString(2,codigo);
            if(ps.executeUpdate()==0) throw new IllegalArgumentException("No existe el equipo indicado.");
        } catch(SQLException ex) { throw JdbcUtil.error("actualizar el estado del equipo",ex); }
    }
    @Override public boolean existeCodigo(String codigo) {
        String sql="SELECT 1 FROM equipo WHERE codigo=?";
        try(Connection con=ConexionBD.obtenerConexion(); PreparedStatement ps=con.prepareStatement(sql)) {
            ps.setString(1,codigo); try(ResultSet rs=ps.executeQuery()) { return rs.next(); }
        } catch(SQLException ex) { throw JdbcUtil.error("verificar el equipo",ex); }
    }
}