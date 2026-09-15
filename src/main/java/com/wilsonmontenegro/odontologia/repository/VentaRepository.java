package com.wilsonmontenegro.odontologia.repository;

import com.wilsonmontenegro.odontologia.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import com.wilsonmontenegro.odontologia.model.enums.EstadoVenta;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    @Query("SELECT v FROM Venta v " +
           "JOIN FETCH v.producto " +
           "LEFT JOIN FETCH v.comprador " +
           "ORDER BY v.createdAt DESC")
    List<Venta> findAllConProductoYComprador();

    @Query("""
           SELECT v FROM Venta v
           JOIN FETCH v.producto p
           LEFT JOIN FETCH v.comprador c
           WHERE (:productoId IS NULL OR p.idInventario = :productoId)
           AND (:estado IS NULL OR v.estado = :estado)
           AND (:fechaDesde IS NULL OR v.createdAt >= :fechaDesde)
           AND (:fechaHasta IS NULL OR v.createdAt <= :fechaHasta)
           AND (:busqueda = ''
                OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                OR LOWER(c.name) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                OR LOWER(c.email) LIKE LOWER(CONCAT('%', :busqueda, '%')))
           ORDER BY v.createdAt DESC
           """)
    List<Venta> buscarConFiltros(@Param("productoId") Long productoId,
            @Param("estado") EstadoVenta estado,
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta,
            @Param("busqueda") String busqueda);

    @Query("SELECT v FROM Venta v " +
           "JOIN FETCH v.producto " +
           "LEFT JOIN FETCH v.comprador " +
           "WHERE v.idVenta = :id")
    Optional<Venta> findByIdConProductoYComprador(@Param("id") Long id);
}
