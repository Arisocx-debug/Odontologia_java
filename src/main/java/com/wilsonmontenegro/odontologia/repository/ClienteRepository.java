package com.wilsonmontenegro.odontologia.repository;

import com.wilsonmontenegro.odontologia.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioId(Long usuarioId);
}
