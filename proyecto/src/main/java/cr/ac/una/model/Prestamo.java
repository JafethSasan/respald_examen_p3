/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cr.ac.una.model;

/**
 *
 * @author jafet
 */
import java.time.LocalDate;

public class Prestamo {
    private int id;
    private Solicitante solicitante;
    private Equipo equipo;
    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucionEsperada;
    private LocalDate fechaDevolucionReal;
    private EstadoPrestamo estado;
    private int renovaciones;

    public Prestamo() {
        this.renovaciones = 0;
    }

    public Prestamo(int id, Solicitante solicitante, Equipo equipo,
                    LocalDate fechaPrestamo, LocalDate fechaDevolucionEsperada,
                    LocalDate fechaDevolucionReal, EstadoPrestamo estado) {
        this(id, solicitante, equipo, fechaPrestamo,
                fechaDevolucionEsperada, fechaDevolucionReal, estado, 0);
    }

    public Prestamo(int id, Solicitante solicitante, Equipo equipo,
                    LocalDate fechaPrestamo, LocalDate fechaDevolucionEsperada,
                    LocalDate fechaDevolucionReal, EstadoPrestamo estado,
                    int renovaciones) {
        this.id = id;
        this.solicitante = solicitante;
        this.equipo = equipo;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucionEsperada = fechaDevolucionEsperada;
        this.fechaDevolucionReal = fechaDevolucionReal;
        this.estado = estado;
        this.renovaciones = renovaciones;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Solicitante getSolicitante() { return solicitante; }
    public void setSolicitante(Solicitante solicitante) { this.solicitante = solicitante; }
    public Equipo getEquipo() { return equipo; }
    public void setEquipo(Equipo equipo) { this.equipo = equipo; }
    public LocalDate getFechaPrestamo() { return fechaPrestamo; }
    public void setFechaPrestamo(LocalDate fechaPrestamo) { this.fechaPrestamo = fechaPrestamo; }
    public LocalDate getFechaDevolucionEsperada() { return fechaDevolucionEsperada; }
    public void setFechaDevolucionEsperada(LocalDate fecha) { this.fechaDevolucionEsperada = fecha; }
    public LocalDate getFechaDevolucionReal() { return fechaDevolucionReal; }
    public void setFechaDevolucionReal(LocalDate fecha) { this.fechaDevolucionReal = fecha; }
    public EstadoPrestamo getEstado() { return estado; }
    public void setEstado(EstadoPrestamo estado) { this.estado = estado; }
    public int getRenovaciones() { return renovaciones; }
    public void setRenovaciones(int renovaciones) {
        if (renovaciones < 0) throw new IllegalArgumentException("Las renovaciones no pueden ser negativas.");
        this.renovaciones = renovaciones;
    }

    @Override
    public String toString() {
        return "Prestamo{" + "id=" + id
                + ", solicitante=" + solicitante
                + ", equipo=" + equipo
                + ", fechaPrestamo=" + fechaPrestamo
                + ", fechaDevolucionEsperada=" + fechaDevolucionEsperada
                + ", fechaDevolucionReal=" + fechaDevolucionReal
                + ", estado=" + estado
                + ", renovaciones=" + renovaciones + '}';
    }
}

