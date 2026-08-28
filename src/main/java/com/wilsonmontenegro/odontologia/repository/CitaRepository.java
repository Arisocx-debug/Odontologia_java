package com.wilsonmontenegro.odontologia.repository;

import com.wilsonmontenegro.odontologia.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CitaRepository extends JpaRepository<Cita, Long> {

        /**
         * Todas las citas de un cliente en particular (portal del cliente), mas
         * recientes primero.
         */
        @Query("""
                        SELECT c FROM Cita c
                        LEFT JOIN FETCH c.servicio
                        WHERE c.cliente.usuario.id = :usuarioId
                        ORDER BY c.fechaEntrada DESC
                        """)
        List<Cita> findByUsuarioIdOrderByFechaEntradaDesc(
                        @Param("usuarioId") Long usuarioId);

        /** Todas las citas ordenadas por fecha (vista admin/empleado). */
        @Query("""
                        SELECT c FROM Cita c
                        LEFT JOIN FETCH c.cliente cl
                        LEFT JOIN FETCH cl.usuario
                        LEFT JOIN FETCH c.servicio
                        ORDER BY c.fechaEntrada DESC
                        """)
        List<Cita> findAllByOrderByFechaEntradaDesc();

        /**
         * Busqueda por nombre de cliente, correo, nombre de servicio o estado.
         * Equivalente al JOIN + LIKE usado en
         * AdminCitaController/EmpleadoCitaController.
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
         * Verifica si hay solapamiento de horario con otra cita existente.
         * Devuelve la maxima fecha_salida entre las citas en conflicto (para informar
         * "disponible desde"), igual que el metodo verificarSolapamiento() de Laravel.
         */
        @Query("""
                        SELECT MAX(c.fechaSalida) FROM Cita c
                        WHERE (:entrada < c.fechaSalida) AND (:salida > c.fechaEntrada)
                        AND (:excluirId IS NULL OR c.idCita <> :excluirId)
                        """)
        Optional<LocalDateTime> buscarSolapamiento(
                        @Param("entrada") LocalDateTime entrada,
                        @Param("salida") LocalDateTime salida,
                        @Param("excluirId") Long excluirId);

        long countByFechaEntradaBetween(LocalDateTime desde, LocalDateTime hasta);

        @Query("SELECT COUNT(c) FROM Cita c WHERE c.fechaEntrada BETWEEN :desde AND :hasta AND c.estado = :estado")
        long countByFechaEntradaBetweenAndEstado(
                        @Param("desde") LocalDateTime desde,
                        @Param("hasta") LocalDateTime hasta,
                        @Param("estado") com.wilsonmontenegro.odontologia.model.enums.EstadoCita estado);

        @Query("""
                        SELECT COALESCE(SUM(c.servicio.costo), 0) FROM Cita c
                        WHERE c.fechaEntrada BETWEEN :desde AND :hasta AND c.estado = 'ATENDIDA'
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
        Optional<Cita> findByIdConRelaciones(@Param("id") Long id);

        long countByClienteUsuarioId(Long usuarioId);

        long countByServicioIdServicio(Long idServicio);
}
