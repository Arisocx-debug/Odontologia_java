package com.wilsonmontenegro.odontologia.model;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import com.wilsonmontenegro.odontologia.model.enums.EstadoInventario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    /**
     * Control de concurrencia.
     * Evita que dos compras simultáneas trabajen
     * sobre el mismo stock sin control.
     */
    @Version
    private Long version;

    @Column(length = 50)
    private String nombre;

    @Column(nullable = false)
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

    @Column(name = "imagen", length = 255)
    private String imagen;

    @Column(name = "ultima_actualizacion")
    private LocalDateTime ultimaActualizacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoInventario estado = EstadoInventario.ACTIVO;
    @Transient
    public String getImagenUrl() {

        if (imagen == null || imagen.isBlank()) {
            return "/img/producto-default.svg";
        }

        String valor = imagen.trim().replace("\\", "/");

        if (valor.startsWith("http://") || valor.startsWith("https://") || valor.startsWith("/")) {
            return agregarCacheBusting(valor);
        }

        int staticImgIndex = valor.indexOf("static/img/");
        if (staticImgIndex >= 0) {
            return agregarCacheBusting("/img/" + codificarSegmentos(valor.substring(staticImgIndex + "static/img/".length())));
        }

        int imgIndex = valor.indexOf("img/");
        if (imgIndex >= 0) {
            return agregarCacheBusting("/img/" + codificarSegmentos(valor.substring(imgIndex + "img/".length())));
        }

        if (valor.startsWith("uploads/")) {
            return agregarCacheBusting("/" + codificarSegmentos(valor));
        }

        return agregarCacheBusting("/img/" + codificarSegmentos(valor));
    }

    /**
     * Agrega un parámetro de cache-busting a la URL de la imagen.
     * Esto fuerza al navegador a cargar la nueva imagen cuando se actualiza.
     */
    private String agregarCacheBusting(String url) {
        // Usar la fecha de actualización como versión, o un timestamp actual si no hay fecha
        String version = ultimaActualizacion != null
                ? String.valueOf(ultimaActualizacion.toEpochSecond(java.time.ZoneOffset.UTC))
                : String.valueOf(System.currentTimeMillis());

        String separator = url.contains("?") ? "&" : "?";
        return url + separator + "v=" + version;
    }

    private String codificarSegmentos(String ruta) {

        String[] segmentos = ruta.split("/");
        StringBuilder resultado = new StringBuilder();

        for (String segmento : segmentos) {
            if (segmento.isBlank()) {
                continue;
            }

            if (resultado.length() > 0) {
                resultado.append('/');
            }

            resultado.append(
                    URLEncoder.encode(segmento, StandardCharsets.UTF_8)
                            .replace("+", "%20")
            );
        }

        return resultado.toString();
    }
}