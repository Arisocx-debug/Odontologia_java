package com.wilsonmontenegro.odontologia.repository;

import com.wilsonmontenegro.odontologia.model.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    java.util.Optional<Inventario> findById(Long id);

    @Query("""
            SELECT i FROM Inventario i
            WHERE LOWER(i.nombre) LIKE LOWER(CONCAT('%', :buscar, '%'))
               OR LOWER(i.nombreProveedor) LIKE LOWER(CONCAT('%', :buscar, '%'))
            """)
    List<Inventario> buscar(@Param("buscar") String buscar);
}
