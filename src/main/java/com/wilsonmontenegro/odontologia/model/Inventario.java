package com.wilsonmontenegro.odontologia.model;

import com.wilsonmontenegro.odontologia.model.enums.EstadoInventario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad Inventario. Equivalente al modelo Inventario.php (tabla `inventario`) de Laravel.
 * Es la entidad que realmente maneja el stock disponible para la venta.
 */
@Entity
@Table(name = "inventario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idinventario")
    private Long idInventario;

    @Version
    private Long version;

    @Column(length = 50)
    private String nombre;

    private Integer stock;

    @Column(name = "precio_unitario", precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "nombre_proveedor", length = 50)
    private String nombreProveedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idproducto")
    private Producto producto;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "ultima_actualizacion")
    private LocalDateTime ultimaActualizacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoInventario estado = EstadoInventario.ACTIVO;
}
