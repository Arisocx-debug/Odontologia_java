package com.wilsonmontenegro.odontologia.repository;

import com.wilsonmontenegro.odontologia.model.Usuario;
import com.wilsonmontenegro.odontologia.model.enums.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRol(Rol rol);

    @Query("""
            SELECT u FROM Usuario u
            WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
               OR u.telefono LIKE CONCAT('%', :search, '%')
               OR LOWER(CAST(u.rol AS string)) LIKE LOWER(CONCAT('%', :search, '%'))
            ORDER BY u.id DESC
            """)
    List<Usuario> buscar(@Param("search") String search);
}
