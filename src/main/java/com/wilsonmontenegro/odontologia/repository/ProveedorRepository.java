package com.wilsonmontenegro.odontologia.repository;

import com.wilsonmontenegro.odontologia.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    Optional<Proveedor> findByNombreIgnoreCase(String nombre);
}
