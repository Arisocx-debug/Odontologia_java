package com.wilsonmontenegro.odontologia.repository;

import com.wilsonmontenegro.odontologia.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    List<Venta> findAllByOrderByCreatedAtDesc();
}
