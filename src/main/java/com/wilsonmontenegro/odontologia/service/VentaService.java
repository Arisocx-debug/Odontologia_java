package com.wilsonmontenegro.odontologia.service;

import com.wilsonmontenegro.odontologia.exception.BusinessException;
import com.wilsonmontenegro.odontologia.exception.RecursoNoEncontradoException;
import com.wilsonmontenegro.odontologia.model.Inventario;
import com.wilsonmontenegro.odontologia.model.MovimientoStock;
import com.wilsonmontenegro.odontologia.model.Venta;
import com.wilsonmontenegro.odontologia.model.Usuario;
import com.wilsonmontenegro.odontologia.model.enums.EstadoInventario;
import com.wilsonmontenegro.odontologia.model.enums.TipoMovimiento;
import com.wilsonmontenegro.odontologia.repository.InventarioRepository;
import com.wilsonmontenegro.odontologia.repository.VentaRepository;
import com.wilsonmontenegro.odontologia.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Logica de negocio de Ventas. Equivalente a VentaController.php y ClienteVentaController.php
 * (parte de registro de venta + descuento stock + movimiento de stock).
 */
@Service
@RequiredArgsConstructor
public class VentaService {

    private final VentaRepository ventaRepository;
    private final InventarioRepository inventarioRepository;
    private final MovimientoStockService movimientoStockService;
    private final UsuarioRepository usuarioRepository;

    public List<Venta> listarTodas() {
        return ventaRepository.findAllByOrderByCreatedAtDesc();
    }

    public Venta obtenerPorId(Long id) {
        return ventaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Venta no encontrada"));
    }

    /**
     * Registrar una venta (usada tanto por el modulo admin/empleado con descuento general,
     * como por el checkout del cliente). Descuenta stock y registra el movimiento.
     */
    @Transactional
    public Venta registrarVenta(Long idProducto, int cantidad, BigDecimal descuento, String responsable,
                               String origenMovimiento, Long compradorId) {
        if (cantidad <= 0) {
            throw new BusinessException("La cantidad debe ser mayor que cero.");
        }
        Inventario producto = inventarioRepository.findById(idProducto)
                .orElseThrow(() -> new BusinessException("Producto no encontrado."));
        if (producto.getEstado() != EstadoInventario.ACTIVO) {
            throw new BusinessException("El producto no esta disponible para la venta.");
        }
        if (cantidad > producto.getStock()) {
            throw new BusinessException("No hay suficiente stock de " + producto.getNombre() + ".");
        }

        BigDecimal subtotal = producto.getPrecioUnitario().multiply(BigDecimal.valueOf(cantidad));
        BigDecimal descuentoAplicado = descuento != null ? descuento : BigDecimal.ZERO;
        if (descuentoAplicado.signum() < 0 || descuentoAplicado.compareTo(subtotal) > 0) {
            throw new BusinessException("El descuento debe estar entre cero y el subtotal.");
        }
        BigDecimal total = subtotal.subtract(descuentoAplicado).max(BigDecimal.ZERO);

        Usuario comprador = null;
        if (compradorId != null) {
            comprador = usuarioRepository.findById(compradorId)
                    .orElseThrow(() -> new BusinessException("Comprador no encontrado."));
        }

        Venta venta = Venta.builder()
                .producto(producto)
                .comprador(comprador)
                .cantidad(cantidad)
                .subtotal(subtotal)
                .descuento(descuentoAplicado)
                .total(total)
                .build();
        venta = ventaRepository.save(venta);

        // Descontar stock
        producto.setStock(producto.getStock() - cantidad);
        producto.setUltimaActualizacion(java.time.LocalDateTime.now());
        inventarioRepository.save(producto);

        // Registrar movimiento de stock (equivalente a MovimientoStock::create(...))
        movimientoStockService.registrar(
                producto.getIdInventario(),
                producto.getNombre(),
                TipoMovimiento.SALIDA,
                cantidad,
                origenMovimiento,
                responsable
        );

        return venta;
    }

    @Transactional
    public void eliminar(Long id) {
        Venta venta = obtenerPorId(id);
        Inventario producto = inventarioRepository.findById(venta.getProducto().getIdInventario())
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto de inventario no encontrado"));
        producto.setStock(producto.getStock() + venta.getCantidad());
        producto.setUltimaActualizacion(java.time.LocalDateTime.now());
        inventarioRepository.save(producto);
        movimientoStockService.registrar(producto.getIdInventario(), producto.getNombre(), TipoMovimiento.ENTRADA,
                venta.getCantidad(), "ANULACION_VENTA", "Sistema");
        ventaRepository.delete(venta);
    }

    public void validarPropietario(Venta venta, Long usuarioId) {
        if (venta.getComprador() == null || !venta.getComprador().getId().equals(usuarioId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "No tienes permiso sobre esta compra.");
        }
    }

    public List<Venta> reporte() {
        return ventaRepository.findAll();
    }
}
