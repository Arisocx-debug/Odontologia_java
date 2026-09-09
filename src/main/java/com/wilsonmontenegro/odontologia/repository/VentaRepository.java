package com.wilsonmontenegro.odontologia.repository;

import com.wilsonmontenegro.odontologia.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    @Query("SELECT v FROM Venta v " +
           "JOIN FETCH v.producto " +
           "LEFT JOIN FETCH v.comprador " +
           "ORDER BY v.createdAt DESC")
    List<Venta> findAllConProductoYComprador();

    @Query("SELECT v FROM Venta v " +
           "JOIN FETCH v.producto " +
           "LEFT JOIN FETCH v.comprador " +
           "WHERE v.idVenta = :id")
    Optional<Venta> findByIdConProductoYComprador(@Param("id") Long id);
}