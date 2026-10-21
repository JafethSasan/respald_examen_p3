-- ==========================================================
-- SISTEMA DE PRESTAMO DE EQUIPOS TECNOLOGICOS
-- Caso 1 - Programacion III
-- Compatible con importacion desde phpMyAdmin
-- ==========================================================
-- INSTRUCCIONES:
-- 1. Cree una base de datos llamada examen_progra3 en phpMyAdmin.
-- 2. Seleccione esa base de datos.
-- 3. Abra la pestaña Importar y cargue este archivo.
-- 4. Este script no elimina ni crea bases de datos.
--
-- NOTA:
-- Si las tablas ya existen, elimínelas manualmente desde phpMyAdmin
-- solo si está seguro de que puede borrar sus datos.
-- ==========================================================

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS solicitante (
    identificacion VARCHAR(30) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    correo VARCHAR(120) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    PRIMARY KEY (identificacion),
    UNIQUE KEY uq_solicitante_correo (correo),
    CONSTRAINT chk_solicitante_tipo
        CHECK (tipo IN ('ESTUDIANTE', 'FUNCIONARIO'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS equipo (
    codigo VARCHAR(20) NOT NULL,
    descripcion VARCHAR(150) NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'DISPONIBLE',
    PRIMARY KEY (codigo),
    CONSTRAINT chk_equipo_estado
        CHECK (estado IN ('DISPONIBLE', 'PRESTADO', 'EN_REPARACION'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS prestamo (
    id INT NOT NULL AUTO_INCREMENT,
    solicitante_id VARCHAR(30) NOT NULL,
    equipo_codigo VARCHAR(20) NOT NULL,
    fecha_prestamo DATE NOT NULL,
    fecha_devolucion_esperada DATE NOT NULL,
    fecha_devolucion_real DATE DEFAULT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    PRIMARY KEY (id),
    KEY idx_prestamo_solicitante_estado (solicitante_id, estado),
    KEY idx_prestamo_equipo_estado (equipo_codigo, estado),
    CONSTRAINT fk_prestamo_solicitante
        FOREIGN KEY (solicitante_id)
        REFERENCES solicitante (identificacion)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_prestamo_equipo
        FOREIGN KEY (equipo_codigo)
        REFERENCES equipo (codigo)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT chk_prestamo_estado
        CHECK (estado IN ('ACTIVO', 'DEVUELTO')),
    CONSTRAINT chk_prestamo_fechas
        CHECK (fecha_devolucion_esperada > fecha_prestamo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Datos iniciales: INSERT IGNORE evita duplicar claves ya existentes.
INSERT IGNORE INTO solicitante (identificacion, nombre, correo, tipo)
VALUES
    ('111111111', 'Ana Rodriguez', 'ana@example.com', 'ESTUDIANTE'),
    ('222222222', 'Carlos Fernandez', 'carlos@example.com', 'FUNCIONARIO');

INSERT IGNORE INTO equipo (codigo, descripcion, tipo, estado)
VALUES
    ('EQ-01', 'Computadora portatil Dell', 'COMPUTADORA', 'DISPONIBLE'),
    ('EQ-02', 'Proyector Epson', 'PROYECTOR', 'DISPONIBLE'),
    ('EQ-03', 'Camara digital Canon', 'CAMARA', 'EN_REPARACION');

-- ==========================================================
-- FIN DEL SCRIPT
-- ==========================================================
