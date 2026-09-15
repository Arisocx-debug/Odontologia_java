package com.wilsonmontenegro.odontologia.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.wilsonmontenegro.odontologia.model.Inventario;
import com.wilsonmontenegro.odontologia.model.enums.EstadoInventario;

import jakarta.persistence.LockModeType;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Inventario> findById(Long id);

    @Query("""
            SELECT i FROM Inventario i
            WHERE LOWER(i.nombre) LIKE LOWER(CONCAT('%', :buscar, '%'))
               OR LOWER(i.nombreProveedor) LIKE LOWER(CONCAT('%', :buscar, '%'))
            """)
    List<Inventario> buscar(@Param("buscar") String buscar);

    @Query("""
            SELECT i FROM Inventario i
            WHERE (:proveedor IS NULL OR :proveedor = '' OR i.nombreProveedor = :proveedor)
            AND (:estado IS NULL OR i.estado = :estado)
            AND (:stockMinimo IS NULL OR i.stock >= :stockMinimo)
            AND (:stockMaximo IS NULL OR i.stock <= :stockMaximo)
            AND (:buscar = ''
                OR LOWER(i.nombre) LIKE LOWER(CONCAT('%', :buscar, '%'))
                OR LOWER(i.nombreProveedor) LIKE LOWER(CONCAT('%', :buscar, '%'))
                OR LOWER(i.descripcion) LIKE LOWER(CONCAT('%', :buscar, '%')))
            ORDER BY i.nombre ASC
            """)
    List<Inventario> buscarConFiltros(@Param("buscar") String buscar,
            @Param("proveedor") String proveedor,
            @Param("estado") EstadoInventario estado,
            @Param("stockMinimo") Integer stockMinimo,
            @Param("stockMaximo") Integer stockMaximo);

    // MÉTODO QUE FALTABA
    List<Inventario> findByEstado(EstadoInventario estado);
}
