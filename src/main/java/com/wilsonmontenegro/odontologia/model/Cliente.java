package com.wilsonmontenegro.odontologia.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad Cliente. Equivalente al modelo Cliente.php (tabla `cliente`) de Laravel.
 * Relaciona 1 a 1 con Usuario (todo cliente tiene un usuario asociado con rol CLIENTE).
 */
@Entity
@Table(name = "cliente")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idcliente")
    private Long idCliente;

    /** FK hacia users.id (columna `id` en la tabla original de Laravel). */
    @OneToOne
    @JoinColumn(name = "id", referencedColumnName = "id")
    private Usuario usuario;

    /** Servicio de preferencia/asociado (columna heredada del esquema original, opcional). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idservicio")
    private Servicio servicio;
}
