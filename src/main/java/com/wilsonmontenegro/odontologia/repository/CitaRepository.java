package com.wilsonmontenegro.odontologia.repository;

import com.wilsonmontenegro.odontologia.model.Cita;
import com.wilsonmontenegro.odontologia.model.enums.EstadoCita;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    /**
     * Todas las citas de un cliente.
     */
    @Query("""
            SELECT c FROM Cita c
            LEFT JOIN FETCH c.servicio
            WHERE c.cliente.usuario.id = :usuarioId
            ORDER BY c.fechaEntrada DESC
            """)
    List<Cita> findByUsuarioIdOrderByFechaEntradaDesc(
            @Param("usuarioId") Long usuarioId);

    /**
     * Todas las citas ordenadas por fecha.
     */
    @Query("""
            SELECT c FROM Cita c
            LEFT JOIN FETCH c.cliente cl
            LEFT JOIN FETCH cl.usuario
            LEFT JOIN FETCH c.servicio
            ORDER BY c.fechaEntrada DESC
            """)
    List<Cita> findAllByOrderByFechaEntradaDesc();

    /**
     * Búsqueda general.
     */
    @Query("""
            SELECT DISTINCT c FROM Cita c
            LEFT JOIN FETCH c.cliente cl
            LEFT JOIN FETCH cl.usuario
            LEFT JOIN FETCH c.servicio
            WHERE (
                LOWER(c.cliente.usuario.name) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                OR LOWER(c.cliente.usuario.email) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                OR LOWER(c.servicio.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                OR LOWER(CAST(c.estado AS string)) LIKE LOWER(CONCAT('%', :busqueda, '%'))
            )
            ORDER BY c.fechaEntrada DESC
            """)
    List<Cita> buscar(@Param("busqueda") String busqueda);

    /**
     * Solapamiento de citas.
     */
    @Query("""
            SELECT MAX(c.fechaSalida) FROM Cita c
            WHERE (:entrada < c.fechaSalida)
            AND (:salida > c.fechaEntrada)
            AND (:excluirId IS NULL OR c.idCita <> :excluirId)
            """)
    Optional<LocalDateTime> buscarSolapamiento(
            @Param("entrada") LocalDateTime entrada,
            @Param("salida") LocalDateTime salida,
            @Param("excluirId") Long excluirId);

    /*
     * ============================================================
     * CITAS ACTIVAS
     * ============================================================
     */

    /**
     * Citas activas para administrador/empleado.
     *
     * PENDIENTE y CONFIRMADA siempre permanecen aquí.
     *
     * ATENDIDA y CANCELADA permanecen aquí durante 2 días.
     */
    @Query("""
            SELECT DISTINCT c FROM Cita c
            LEFT JOIN FETCH c.cliente cl
            LEFT JOIN FETCH cl.usuario
            LEFT JOIN FETCH c.servicio
            WHERE
                c.estado IN ('PENDIENTE', 'CONFIRMADA')
                OR (
                    c.estado IN ('ATENDIDA', 'CANCELADA')
                    AND c.fechaEntrada >= :limite
                )
            ORDER BY c.fechaEntrada DESC
            """)
    List<Cita> findCitasActivas(
            @Param("limite") LocalDateTime limite);

    /**
     * Citas activas de un cliente.
     */
    @Query("""
            SELECT DISTINCT c FROM Cita c
            LEFT JOIN FETCH c.servicio
            WHERE c.cliente.usuario.id = :usuarioId
            AND (
                c.estado IN ('PENDIENTE', 'CONFIRMADA')
                OR (
                    c.estado IN ('ATENDIDA', 'CANCELADA')
                    AND c.fechaEntrada >= :limite
                )
            )
            ORDER BY c.fechaEntrada DESC
            """)
    List<Cita> findCitasActivasPorUsuario(
            @Param("usuarioId") Long usuarioId,
            @Param("limite") LocalDateTime limite);

    /*
     * ============================================================
     * HISTORIAL
     * ============================================================
     */

    /**
     * Historial general.
     *
     * Solo ATENDIDA y CANCELADA con más de 2 días.
     */
    @Query("""
            SELECT DISTINCT c FROM Cita c
            LEFT JOIN FETCH c.cliente cl
            LEFT JOIN FETCH cl.usuario
            LEFT JOIN FETCH c.servicio
            WHERE c.estado IN ('ATENDIDA', 'CANCELADA')
            AND c.fechaEntrada < :limite
            ORDER BY c.fechaEntrada DESC
            """)
    List<Cita> findHistorial(
            @Param("limite") LocalDateTime limite);

    /**
     * Historial de un cliente.
     */
    @Query("""
            SELECT DISTINCT c FROM Cita c
            LEFT JOIN FETCH c.servicio
            WHERE c.cliente.usuario.id = :usuarioId
            AND c.estado IN ('ATENDIDA', 'CANCELADA')
            AND c.fechaEntrada < :limite
            ORDER BY c.fechaEntrada DESC
            """)
    List<Cita> findHistorialPorUsuario(
            @Param("usuarioId") Long usuarioId,
            @Param("limite") LocalDateTime limite);

    /**
     * Historial con filtros.
     *
     * clienteId puede ser null.
     * fechaDesde puede ser null.
     * fechaHasta puede ser null.
     * estado puede ser null.
     * búsqueda puede estar vacía.
     */
    @Query("""
            SELECT DISTINCT c FROM Cita c
            LEFT JOIN FETCH c.cliente cl
            LEFT JOIN FETCH cl.usuario
            LEFT JOIN FETCH c.servicio
            WHERE c.estado IN ('ATENDIDA', 'CANCELADA')
            AND c.fechaEntrada < :limite

            AND (
                :clienteId IS NULL
                OR c.cliente.idCliente = :clienteId
            )

            AND (
                :fechaDesde IS NULL
                OR c.fechaEntrada >= :fechaDesde
            )

            AND (
                :fechaHasta IS NULL
                OR c.fechaEntrada <= :fechaHasta
            )

            AND (
                :estado IS NULL
                OR c.estado = :estado
            )

            AND (
                :busqueda = ''
                OR LOWER(c.cliente.usuario.name)
                    LIKE LOWER(CONCAT('%', :busqueda, '%'))
                OR LOWER(c.cliente.usuario.email)
                    LIKE LOWER(CONCAT('%', :busqueda, '%'))
                OR LOWER(c.servicio.nombre)
                    LIKE LOWER(CONCAT('%', :busqueda, '%'))
            )

            ORDER BY c.fechaEntrada DESC
            """)
    List<Cita> buscarHistorial(
            @Param("limite") LocalDateTime limite,
            @Param("clienteId") Long clienteId,
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta,
            @Param("estado") EstadoCita estado,
            @Param("busqueda") String busqueda);

    @Query("""
            SELECT DISTINCT c FROM Cita c
            LEFT JOIN FETCH c.servicio
            WHERE c.cliente.usuario.id = :usuarioId
            AND c.estado IN ('ATENDIDA', 'CANCELADA')
            AND c.fechaEntrada < :limite

            AND (
                :fechaDesde IS NULL
                OR c.fechaEntrada >= :fechaDesde
            )

            AND (
                :fechaHasta IS NULL
                OR c.fechaEntrada <= :fechaHasta
            )

            AND (
                :estado IS NULL
                OR c.estado = :estado
            )

            AND (
                :busqueda = ''
                OR LOWER(c.servicio.nombre)
                    LIKE LOWER(CONCAT('%', :busqueda, '%'))
            )

            ORDER BY c.fechaEntrada DESC
            """)
    List<Cita> buscarHistorialPorUsuario(
            @Param("usuarioId") Long usuarioId,
            @Param("limite") LocalDateTime limite,
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta,
            @Param("estado") EstadoCita estado,
            @Param("busqueda") String busqueda);

    long countByFechaEntradaBetween(
            LocalDateTime desde,
            LocalDateTime hasta);

    @Query("""
            SELECT COUNT(c) FROM Cita c
            WHERE c.fechaEntrada BETWEEN :desde AND :hasta
            AND c.estado = :estado
            """)
    long countByFechaEntradaBetweenAndEstado(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("estado") EstadoCita estado);

    @Query("""
            SELECT COALESCE(SUM(c.servicio.costo), 0)
            FROM Cita c
            WHERE c.fechaEntrada BETWEEN :desde AND :hasta
            AND c.estado = 'ATENDIDA'
            """)
    java.math.BigDecimal sumIngresosEntreFechas(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    @Query("""
            SELECT c FROM Cita c
            JOIN FETCH c.cliente cl
            JOIN FETCH cl.usuario u
            LEFT JOIN FETCH c.servicio s
            WHERE c.idCita = :id
            """)
    Optional<Cita> findByIdConRelaciones(
            @Param("id") Long id);

    long countByClienteUsuarioId(Long usuarioId);

    long countByServicioIdServicio(Long idServicio);
}