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
import cr.ac.una.exception.EquipoNoDisponibleException;
import cr.ac.una.model.*;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Consultas y transacciones JDBC relacionadas con préstamos. */
public class PrestamoRepositoryJdbc implements PrestamoRepository {
    private static final String SELECT_JOIN =
            "SELECT p.id,p.fecha_prestamo,p.fecha_devolucion_esperada,"
            + "p.fecha_devolucion_real,p.estado AS estado_prestamo,p.renovaciones, "
            + "s.identificacion,s.nombre,s.correo,s.tipo AS tipo_solicitante, "
            + "e.codigo,e.descripcion,e.tipo AS tipo_equipo,e.estado AS estado_equipo "
            + "FROM prestamo p JOIN solicitante s ON s.identificacion=p.solicitante_id "
            + "JOIN equipo e ON e.codigo=p.equipo_codigo ";

    private Prestamo mapear(ResultSet rs) throws SQLException {
        Solicitante s = new Solicitante(
                rs.getString("identificacion"), rs.getString("nombre"),
                rs.getString("correo"),
                TipoSolicitante.valueOf(rs.getString("tipo_solicitante")));
        Equipo e = new Equipo(
                rs.getString("codigo"), rs.getString("descripcion"),
                rs.getString("tipo_equipo"),
                EstadoEquipo.valueOf(rs.getString("estado_equipo")));
        Date real = rs.getDate("fecha_devolucion_real");
        return new Prestamo(
                rs.getInt("id"), s, e,
                rs.getDate("fecha_prestamo").toLocalDate(),
                rs.getDate("fecha_devolucion_esperada").toLocalDate(),
                real == null ? null : real.toLocalDate(),
                EstadoPrestamo.valueOf(rs.getString("estado_prestamo")),
                rs.getInt("renovaciones"));
    }

    @Override
    public Optional<Prestamo> buscarPorId(int id) {
        String sql = SELECT_JOIN + "WHERE p.id=?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw JdbcUtil.error("consultar el préstamo", ex);
        }
    }

    @Override
    public List<Prestamo> listar() {
        return consultarLista(SELECT_JOIN + "ORDER BY p.id DESC", null);
    }

    @Override
    public List<Prestamo> listarActivosPorSolicitante(String id) {
        return consultarLista(SELECT_JOIN
                + "WHERE p.solicitante_id=? AND p.estado='ACTIVO' "
                + "ORDER BY p.fecha_prestamo DESC", id);
    }

    private List<Prestamo> consultarLista(String sql, String id) {
        List<Prestamo> lista = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (id != null) ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException ex) {
            throw JdbcUtil.error("consultar préstamos", ex);
        }
        return lista;
    }

    @Override
    public int contarActivosPorSolicitante(String id) {
        String sql = "SELECT COUNT(*) FROM prestamo WHERE solicitante_id=? AND estado='ACTIVO'";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            throw JdbcUtil.error("contar préstamos activos", ex);
        }
    }

    @Override
    public void registrarPrestamo(Prestamo p) {
        String insert = "INSERT INTO prestamo "
                + "(solicitante_id,equipo_codigo,fecha_prestamo,fecha_devolucion_esperada,"
                + "fecha_devolucion_real,estado,renovaciones) VALUES (?,?,?,?,NULL,'ACTIVO',0)";
        String bloquearEquipo = "SELECT estado FROM equipo WHERE codigo=? FOR UPDATE";
        String cambiarEquipo = "UPDATE equipo SET estado='PRESTADO' WHERE codigo=? AND estado='DISPONIBLE'";
        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement lock = con.prepareStatement(bloquearEquipo)) {
                    lock.setString(1, p.getEquipo().getCodigo());
                    try (ResultSet rs = lock.executeQuery()) {
                        if (!rs.next()) throw new DatosException("El equipo ya no existe.", null);
                        if (!EstadoEquipo.DISPONIBLE.name().equals(rs.getString(1)))
                            throw new EquipoNoDisponibleException("El equipo no está disponible para préstamo.");
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(insert, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, p.getSolicitante().getIdentificacion());
                    ps.setString(2, p.getEquipo().getCodigo());
                    ps.setDate(3, Date.valueOf(p.getFechaPrestamo()));
                    ps.setDate(4, Date.valueOf(p.getFechaDevolucionEsperada()));
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) p.setId(keys.getInt(1));
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(cambiarEquipo)) {
                    ps.setString(1, p.getEquipo().getCodigo());
                    if (ps.executeUpdate() != 1)
                        throw new EquipoNoDisponibleException("El equipo dejó de estar disponible durante el registro.");
                }
                con.commit();
            } catch (RuntimeException | SQLException ex) {
                try { con.rollback(); } catch (SQLException rollback) { ex.addSuppressed(rollback); }
                if (ex instanceof RuntimeException re) throw re;
                throw (SQLException) ex;
            } finally {
                try { con.setAutoCommit(true); } catch (SQLException ignored) { }
            }
        } catch (SQLException ex) {
            throw JdbcUtil.error("registrar el préstamo", ex);
        }
    }

    @Override
    public void registrarDevolucion(int id, LocalDate fechaReal) {
        String buscar = "SELECT equipo_codigo,estado FROM prestamo WHERE id=? FOR UPDATE";
        String updatePrestamo = "UPDATE prestamo SET estado='DEVUELTO',fecha_devolucion_real=? WHERE id=? AND estado='ACTIVO'";
        String updateEquipo = "UPDATE equipo SET estado='DISPONIBLE' WHERE codigo=?";
        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                String codigo;
                try (PreparedStatement ps = con.prepareStatement(buscar)) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException("No existe el préstamo indicado.");
                        if (!"ACTIVO".equals(rs.getString("estado"))) throw new IllegalArgumentException("El préstamo ya fue devuelto.");
                        codigo = rs.getString("equipo_codigo");
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(updatePrestamo)) {
                    ps.setDate(1, Date.valueOf(fechaReal));
                    ps.setInt(2, id);
                    if (ps.executeUpdate() != 1) throw new IllegalStateException("No fue posible actualizar el préstamo.");
                }
                try (PreparedStatement ps = con.prepareStatement(updateEquipo)) {
                    ps.setString(1, codigo);
                    ps.executeUpdate();
                }
                con.commit();
            } catch (RuntimeException | SQLException ex) {
                try { con.rollback(); } catch (SQLException rollback) { ex.addSuppressed(rollback); }
                if (ex instanceof RuntimeException re) throw re;
                throw (SQLException) ex;
            } finally {
                try { con.setAutoCommit(true); } catch (SQLException ignored) { }
            }
        } catch (SQLException ex) {
            throw JdbcUtil.error("registrar la devolución", ex);
        }
    }

    @Override
    public boolean renovarPrestamo(int id, LocalDate nuevaFechaEsperada) {
        String sql = "UPDATE prestamo SET fecha_devolucion_esperada=?, renovaciones=1 "
                + "WHERE id=? AND estado='ACTIVO' AND renovaciones=0 "
                + "AND fecha_devolucion_esperada>=?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(nuevaFechaEsperada));
            ps.setInt(2, id);
            ps.setDate(3, Date.valueOf(LocalDate.now()));
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw JdbcUtil.error("renovar el préstamo", ex);
        }
    }

    @Override
    public List<PrestamoVencido> listarActivosVencidos(LocalDate hoy) {
        String sql = "SELECT p.id, s.nombre, e.codigo, DATEDIFF(?, p.fecha_devolucion_esperada) AS dias_atraso "
                + "FROM prestamo p JOIN solicitante s ON s.identificacion=p.solicitante_id "
                + "JOIN equipo e ON e.codigo=p.equipo_codigo "
                + "WHERE p.estado='ACTIVO' AND p.fecha_devolucion_esperada<? "
                + "ORDER BY p.fecha_devolucion_esperada ASC";
        List<PrestamoVencido> lista = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(hoy));
            ps.setDate(2, Date.valueOf(hoy));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new PrestamoVencido(rs.getInt("id"), rs.getString("nombre"),
                            rs.getString("codigo"), rs.getLong("dias_atraso")));
                }
            }
        } catch (SQLException ex) {
            throw JdbcUtil.error("listar préstamos vencidos", ex);
        }
        return lista;
    }
}