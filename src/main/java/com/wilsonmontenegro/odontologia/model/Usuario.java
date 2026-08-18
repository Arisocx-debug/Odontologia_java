package com.wilsonmontenegro.odontologia.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wilsonmontenegro.odontologia.model.enums.Rol;
import com.wilsonmontenegro.odontologia.model.converter.RolConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad Usuario. Equivalente al modelo User.php (tabla `users`) de Laravel.
 * Incluye administradores, empleados y clientes: el rol determina los permisos.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Convert(converter = RolConverter.class)
    @Column(nullable = false)
    @Builder.Default
    private Rol rol = Rol.CLIENTE;

    @Column(length = 20)
    private String telefono;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
