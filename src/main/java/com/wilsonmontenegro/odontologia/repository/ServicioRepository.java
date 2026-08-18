package com.wilsonmontenegro.odontologia.repository;

import com.wilsonmontenegro.odontologia.model.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    List<Servicio> findAllByOrderByNombreAsc();
}
