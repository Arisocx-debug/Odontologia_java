package com.wilsonmontenegro.odontologia.repository;

import com.wilsonmontenegro.odontologia.model.Usuario;
import com.wilsonmontenegro.odontologia.model.enums.EstadoUsuario;
import com.wilsonmontenegro.odontologia.model.enums.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

        Optional<Usuario> findByEmail(String email);

        boolean existsByEmail(String email);

        long countByRol(Rol rol);

        @Query("""
                        SELECT COUNT(u) FROM Usuario u
                        WHERE u.rol = :rol AND (u.estado IS NULL OR u.estado = :estado)
                        """)
        long countActivosPorRol(@Param("rol") Rol rol, @Param("estado") EstadoUsuario estado);

        @Query("""
                        SELECT u FROM Usuario u
                        WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%'))
                           OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
                           OR u.telefono LIKE CONCAT('%', :search, '%')
                           OR LOWER(CAST(u.rol AS string)) LIKE LOWER(CONCAT('%', :search, '%'))
                        ORDER BY u.id DESC
                        """)
        List<Usuario> buscar(@Param("search") String search);

        @Query("""
                        SELECT u FROM Usuario u
                        WHERE (:rol IS NULL OR u.rol = :rol)
                           AND (:estado IS NULL OR u.estado = :estado)
                           AND (:fechaDesde IS NULL OR u.createdAt >= :fechaDesde)
                           AND (:fechaHasta IS NULL OR u.createdAt <= :fechaHasta)
                           AND (:search = ''
                                OR LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%'))
                                OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
                                OR u.telefono LIKE CONCAT('%', :search, '%'))
                        ORDER BY u.id DESC
                        """)
        List<Usuario> buscarConFiltros(@Param("search") String search,
                        @Param("rol") Rol rol, @Param("estado") EstadoUsuario estado,
                        @Param("fechaDesde") LocalDateTime fechaDesde, @Param("fechaHasta") LocalDateTime fechaHasta);
}
