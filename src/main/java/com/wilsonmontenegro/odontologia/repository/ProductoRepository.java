package com.wilsonmontenegro.odontologia.repository;

import com.wilsonmontenegro.odontologia.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
