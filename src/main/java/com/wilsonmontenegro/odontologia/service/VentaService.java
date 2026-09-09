package com.wilsonmontenegro.odontologia.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wilsonmontenegro.odontologia.dto.VentaDTO;
import com.wilsonmontenegro.odontologia.exception.BusinessException;
import com.wilsonmontenegro.odontologia.exception.RecursoNoEncontradoException;
import com.wilsonmontenegro.odontologia.model.Inventario;
import com.wilsonmontenegro.odontologia.model.Usuario;
import com.wilsonmontenegro.odontologia.model.Venta;
import com.wilsonmontenegro.odontologia.model.enums.EstadoInventario;
import com.wilsonmontenegro.odontologia.model.enums.EstadoVenta;
import com.wilsonmontenegro.odontologia.model.enums.TipoMovimiento;
import com.wilsonmontenegro.odontologia.repository.InventarioRepository;
import com.wilsonmontenegro.odontologia.repository.UsuarioRepository;
import com.wilsonmontenegro.odontologia.repository.VentaRepository;

import lombok.RequiredArgsConstructor;

/**
 * Logica de negocio de Ventas.
 *
 * Se encarga de:
 * - Registrar ventas.
 * - Validar stock.
 * - Descontar inventario.
 * - Registrar movimientos de stock.
 * - Anular ventas.
 * - Reactivar ventas.
 * - Registrar compras realizadas desde el cliente.
 */
@Service
@RequiredArgsConstructor
public class VentaService {

    private final VentaRepository ventaRepository;
    private final InventarioRepository inventarioRepository;
    private final MovimientoStockService movimientoStockService;
    private final UsuarioRepository usuarioRepository;


    // ============================================================
    // LISTAR VENTAS
    // ============================================================

    public List<Venta> listarTodas() {
        return ventaRepository.findAllConProductoYComprador();
    }


    // ============================================================
    // OBTENER VENTA
    // ============================================================

    public Venta obtenerPorId(Long id) {

        if (id == null) {
            throw new BusinessException(
                    "El ID de la venta es obligatorio.");
        }

        return ventaRepository.findByIdConProductoYComprador(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Venta no encontrada."));
    }


    // ============================================================
    // REGISTRAR VENTA
    // ============================================================

    @Transactional
    public Venta registrarVenta(
            Long idProducto,
            int cantidad,
            BigDecimal descuento,
            String responsable,
            String origenMovimiento,
            Long compradorId) {

        // --------------------------------------------------------
        // 1. VALIDAR ID DEL PRODUCTO
        // --------------------------------------------------------

        if (idProducto == null) {
            throw new BusinessException(
                    "El producto es obligatorio.");
        }


        // --------------------------------------------------------
        // 2. VALIDAR CANTIDAD
        // --------------------------------------------------------

        if (cantidad <= 0) {
            throw new BusinessException(
                    "La cantidad debe ser mayor que cero.");
        }


        // --------------------------------------------------------
        // 3. BUSCAR PRODUCTO
        // --------------------------------------------------------

        /*
         * findById() utiliza PESSIMISTIC_WRITE
         * gracias al InventarioRepository.
         *
         * Esto evita que dos compras modifiquen
         * simultaneamente el mismo stock.
         */

        Inventario producto =
                inventarioRepository.findById(idProducto)
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Producto no encontrado."));


        // --------------------------------------------------------
        // 4. VALIDAR ESTADO
        // --------------------------------------------------------

        if (producto.getEstado() != EstadoInventario.ACTIVO) {
            throw new BusinessException(
                    "El producto no está disponible para la venta.");
        }


        // --------------------------------------------------------
        // 5. VALIDAR STOCK
        // --------------------------------------------------------

        if (producto.getStock() == null) {
            throw new BusinessException(
                    "El producto no tiene stock configurado.");
        }

        if (producto.getStock() < 0) {
            throw new BusinessException(
                    "El stock del producto es inválido.");
        }

        if (producto.getStock() < cantidad) {
            throw new BusinessException(
                    "No hay suficiente stock de "
                            + producto.getNombre()
                            + ". Stock disponible: "
                            + producto.getStock());
        }


        // --------------------------------------------------------
        // 6. VALIDAR PRECIO
        // --------------------------------------------------------

        if (producto.getPrecioUnitario() == null) {
            throw new BusinessException(
                    "El producto no tiene un precio configurado.");
        }

        if (producto.getPrecioUnitario().signum() < 0) {
            throw new BusinessException(
                    "El precio del producto no puede ser negativo.");
        }


        // --------------------------------------------------------
        // 7. CALCULAR SUBTOTAL
        // --------------------------------------------------------

        BigDecimal subtotal =
                producto.getPrecioUnitario()
                        .multiply(
                                BigDecimal.valueOf(cantidad)
                        );


        // --------------------------------------------------------
        // 8. VALIDAR DESCUENTO
        // --------------------------------------------------------

        BigDecimal descuentoAplicado =
                descuento != null
                        ? descuento
                        : BigDecimal.ZERO;


        if (descuentoAplicado.signum() < 0) {
            throw new BusinessException(
                    "El descuento no puede ser negativo.");
        }


        if (descuentoAplicado.compareTo(subtotal) > 0) {
            throw new BusinessException(
                    "El descuento no puede ser mayor que el subtotal.");
        }


        // --------------------------------------------------------
        // 9. CALCULAR TOTAL
        // --------------------------------------------------------

        BigDecimal total =
                subtotal.subtract(descuentoAplicado);


        // --------------------------------------------------------
        // 10. BUSCAR COMPRADOR
        // --------------------------------------------------------

        Usuario comprador = null;

        if (compradorId != null) {

            comprador =
                    usuarioRepository.findById(compradorId)
                            .orElseThrow(() ->
                                    new BusinessException(
                                            "Comprador no encontrado."));
        }


        // --------------------------------------------------------
        // 11. CREAR VENTA
        // --------------------------------------------------------

        Venta venta =
                Venta.builder()
                        .producto(producto)
                        .comprador(comprador)
                        .cantidad(cantidad)
                        .subtotal(subtotal)
                        .descuento(descuentoAplicado)
                        .total(total)
                        .estado(EstadoVenta.ACTIVA)
                        .build();


        venta =
                ventaRepository.save(venta);


        // --------------------------------------------------------
        // 12. DESCONTAR STOCK
        // --------------------------------------------------------

        int stockAnterior =
                producto.getStock();

        int nuevoStock =
                stockAnterior - cantidad;


        producto.setStock(nuevoStock);

        producto.setUltimaActualizacion(
                LocalDateTime.now()
        );


        inventarioRepository.save(producto);


        // --------------------------------------------------------
        // 13. REGISTRAR MOVIMIENTO
        // --------------------------------------------------------

        movimientoStockService.registrar(
                producto.getIdInventario(),
                producto.getNombre(),
                TipoMovimiento.SALIDA,
                cantidad,
                origenMovimiento != null
                        ? origenMovimiento
                        : "VENTA",
                responsable != null
                        ? responsable
                        : "Sistema"
        );


        return venta;
    }


    // ============================================================
    // ELIMINAR / ANULAR VENTA
    // ============================================================

    @Transactional
    public void eliminar(Long id) {

        Venta venta =
                obtenerPorId(id);


        if (venta.getProducto() == null) {
            throw new RecursoNoEncontradoException(
                    "La venta no tiene un producto asociado.");
        }


        Inventario producto =
                inventarioRepository.findById(
                        venta.getProducto().getIdInventario()
                )
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Producto de inventario no encontrado."
                        ));


        if (producto.getStock() == null) {
            producto.setStock(0);
        }


        // Devolver stock
        producto.setStock(
                producto.getStock()
                        + venta.getCantidad()
        );


        producto.setUltimaActualizacion(
                LocalDateTime.now()
        );


        inventarioRepository.save(producto);


        // Registrar movimiento
        movimientoStockService.registrar(
                producto.getIdInventario(),
                producto.getNombre(),
                TipoMovimiento.ENTRADA,
                venta.getCantidad(),
                "ANULACION_VENTA",
                "Sistema"
        );


        ventaRepository.delete(venta);
    }


    // ============================================================
    // ACTIVAR / DESACTIVAR VENTA
    // ============================================================

    @Transactional
    public Venta toggleEstado(Long id) {

        Venta venta =
                obtenerPorId(id);


        if (venta.getProducto() == null) {
            throw new RecursoNoEncontradoException(
                    "La venta no tiene un producto asociado.");
        }


        Inventario producto =
                inventarioRepository.findById(
                        venta.getProducto().getIdInventario()
                )
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Producto de inventario no encontrado."
                        ));


        if (producto.getStock() == null) {
            producto.setStock(0);
        }


        // --------------------------------------------------------
        // DESACTIVAR
        // --------------------------------------------------------

        if (venta.getEstado() == EstadoVenta.ACTIVA) {

            producto.setStock(
                    producto.getStock()
                            + venta.getCantidad()
            );


            movimientoStockService.registrar(
                    producto.getIdInventario(),
                    producto.getNombre(),
                    TipoMovimiento.ENTRADA,
                    venta.getCantidad(),
                    "ANULACION_VENTA",
                    "Sistema"
            );


            venta.setEstado(
                    EstadoVenta.INACTIVA
            );

        }


        // --------------------------------------------------------
        // REACTIVAR
        // --------------------------------------------------------

        else {

            if (producto.getEstado()
                    != EstadoInventario.ACTIVO) {

                throw new BusinessException(
                        "El producto no está disponible para reactivar la venta."
                );
            }


            if (producto.getStock()
                    < venta.getCantidad()) {

                throw new BusinessException(
                        "No hay suficiente stock para reactivar esta venta."
                );
            }


            producto.setStock(
                    producto.getStock()
                            - venta.getCantidad()
            );


            movimientoStockService.registrar(
                    producto.getIdInventario(),
                    producto.getNombre(),
                    TipoMovimiento.SALIDA,
                    venta.getCantidad(),
                    "REACTIVACION_VENTA",
                    "Sistema"
            );


            venta.setEstado(
                    EstadoVenta.ACTIVA
            );
        }


        producto.setUltimaActualizacion(
                LocalDateTime.now()
        );


        inventarioRepository.save(producto);


        return ventaRepository.save(venta);
    }


    // ============================================================
    // VALIDAR PROPIETARIO
    // ============================================================

    public void validarPropietario(
            Venta venta,
            Long usuarioId) {

        if (venta == null) {
            throw new AccessDeniedException(
                    "Compra no encontrada.");
        }


        if (usuarioId == null) {
            throw new AccessDeniedException(
                    "Debes iniciar sesión.");
        }


        if (venta.getComprador() == null) {
            throw new AccessDeniedException(
                    "Esta compra no tiene comprador asociado.");
        }


        if (!venta.getComprador()
                .getId()
                .equals(usuarioId)) {

            throw new AccessDeniedException(
                    "No tienes permiso sobre esta compra.");
        }
    }


    // ============================================================
    // REPORTE
    // ============================================================

    public List<Venta> reporte() {

        return ventaRepository
                .findAllConProductoYComprador();
    }


    // ============================================================
    // REGISTRAR COMPRA COMPLETA DEL CARRITO
    // ============================================================

    @Transactional
    public List<Venta> registrarVentasCliente(
            List<VentaDTO> items,
            Long compradorId,
            String responsable) {


        // --------------------------------------------------------
        // 1. VALIDAR CARRITO
        // --------------------------------------------------------

        if (items == null || items.isEmpty()) {
            throw new BusinessException(
                    "El carrito está vacío.");
        }


        // --------------------------------------------------------
        // 2. VALIDAR USUARIO
        // --------------------------------------------------------

        if (compradorId == null) {
            throw new BusinessException(
                    "Debes iniciar sesión para realizar una compra.");
        }


        // --------------------------------------------------------
        // 3. COMPROBAR QUE EL USUARIO EXISTE
        // --------------------------------------------------------

        usuarioRepository.findById(compradorId)
                .orElseThrow(() ->
                        new BusinessException(
                                "El usuario comprador no existe."
                        ));


        // --------------------------------------------------------
        // 4. LISTA DE VENTAS
        // --------------------------------------------------------

        List<Venta> ventas =
                new ArrayList<>();


        // --------------------------------------------------------
        // 5. PROCESAR CADA PRODUCTO
        // --------------------------------------------------------

        for (VentaDTO item : items) {

            if (item == null) {
                throw new BusinessException(
                        "El carrito contiene un producto inválido.");
            }


            if (item.getProductoId() == null) {
                throw new BusinessException(
                        "El producto de la compra es obligatorio.");
            }


            if (item.getCantidad() <= 0) {
                throw new BusinessException(
                        "La cantidad del producto debe ser mayor que cero.");
            }


            Venta venta =
                    registrarVenta(
                            item.getProductoId(),
                            item.getCantidad(),
                            BigDecimal.ZERO,
                            responsable,
                            "COMPRA_CLIENTE",
                            compradorId
                    );


            ventas.add(venta);
        }


        return ventas;
    }


    // ============================================================
    // MÉTODO ANTIGUO
    // ============================================================

    /**
     * Método antiguo conservado únicamente para compatibilidad.
     *
     * No debe utilizarse para registrar compras.
     */
    @Deprecated
    public void registrarVenta(VentaDTO venta) {

        throw new BusinessException(
                "Usa el checkout para registrar compras.");
    }
}
