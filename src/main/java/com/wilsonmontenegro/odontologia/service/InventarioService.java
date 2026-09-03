package com.wilsonmontenegro.odontologia.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wilsonmontenegro.odontologia.exception.BusinessException;
import com.wilsonmontenegro.odontologia.exception.RecursoNoEncontradoException;
import com.wilsonmontenegro.odontologia.model.Inventario;
import com.wilsonmontenegro.odontologia.model.enums.EstadoInventario;
import com.wilsonmontenegro.odontologia.repository.InventarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventarioService {

    private final InventarioRepository inventarioRepository;

    /**
     * Lista todos los productos del inventario.
     */
    public List<Inventario> listarTodos() {
        return inventarioRepository.findAll();
    }

    /**
     * Busca productos por nombre u otros campos
     * definidos en el repository.
     */
    public List<Inventario> buscar(String texto) {

        if (texto == null || texto.isBlank()) {
            return listarTodos();
        }

        return inventarioRepository.buscar(texto.trim());
    }

    /**
     * Obtiene un producto de inventario por su ID.
     */
    public Inventario obtenerPorId(Long id) {

        if (id == null) {
            throw new BusinessException(
                    "El ID del producto es obligatorio."
            );
        }

        return inventarioRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Producto de inventario no encontrado."
                        )
                );
    }

    /**
     * Crea un nuevo producto en el inventario.
     */
    @Transactional
    public Inventario crear(Inventario datos) {

        validarDatos(datos);

        datos.setEstado(EstadoInventario.ACTIVO);
        datos.setUltimaActualizacion(LocalDateTime.now());

        return inventarioRepository.save(datos);
    }

    /**
     * Actualiza los datos de un producto.
     *
     * Esta operación modifica el stock únicamente cuando
     * un administrador o empleado edita manualmente el producto.
     *
     * Las compras NO deben utilizar este método para descontar stock.
     */
    @Transactional
    public Inventario actualizar(Long id, Inventario datos) {

        Inventario item = obtenerPorId(id);

        validarDatos(datos);

        item.setNombre(datos.getNombre());
        item.setStock(datos.getStock());
        item.setPrecioUnitario(datos.getPrecioUnitario());
        item.setNombreProveedor(datos.getNombreProveedor());
        item.setDescripcion(datos.getDescripcion());

        if (datos.getImagen() != null && !datos.getImagen().isBlank()) {
            item.setImagen(datos.getImagen());
        }

        /*
         * No modificamos:
         * - estado
         * - producto
         * - version
         *
         * porque pertenecen al registro existente.
         */

        item.setUltimaActualizacion(LocalDateTime.now());

        return inventarioRepository.save(item);
    }

    /**
     * Elimina un producto del inventario.
     */
    @Transactional
    public void eliminar(Long id) {

        Inventario item = obtenerPorId(id);

        inventarioRepository.delete(item);
    }

    /**
     * Activa o desactiva un producto.
     */
    @Transactional
    public Inventario toggleEstado(Long id) {

        Inventario item = obtenerPorId(id);

        if (item.getEstado() == EstadoInventario.INACTIVO) {
            validarDatos(item);
        }

        if (item.getEstado() == EstadoInventario.ACTIVO) {

            item.setEstado(EstadoInventario.INACTIVO);

        } else {

            item.setEstado(EstadoInventario.ACTIVO);
        }

        item.setUltimaActualizacion(LocalDateTime.now());

        return inventarioRepository.save(item);
    }

    /**
     * Descuenta stock de un producto.
     *
     * IMPORTANTE:
     *
     * Este método es para operaciones internas.
     * La compra del cliente actualmente se procesa desde
     * VentaService.registrarVenta(), donde además se registra
     * la venta y el movimiento de inventario.
     */
    @Transactional
    public void descontarStock(
            Inventario item,
            int cantidad) {

        if (item == null) {
            throw new BusinessException(
                    "El producto de inventario es obligatorio."
            );
        }

        if (cantidad <= 0) {
            throw new BusinessException(
                    "La cantidad a descontar debe ser mayor que cero."
            );
        }

        if (item.getStock() == null) {
            throw new BusinessException(
                    "El producto no tiene un stock definido."
            );
        }

        if (item.getStock() < cantidad) {
            throw new BusinessException(
                    "No hay suficiente stock de "
                            + item.getNombre()
                            + ". Stock disponible: "
                            + item.getStock()
            );
        }

        item.setStock(
                item.getStock() - cantidad
        );

        item.setUltimaActualizacion(
                LocalDateTime.now()
        );

        inventarioRepository.save(item);
    }

    /**
     * Aumenta stock.
     *
     * Se utiliza, por ejemplo, cuando se anula una venta
     * y se devuelve la mercancía al inventario.
     */
    @Transactional
    public void aumentarStock(
            Inventario item,
            int cantidad) {

        if (item == null) {
            throw new BusinessException(
                    "El producto de inventario es obligatorio."
            );
        }

        if (cantidad <= 0) {
            throw new BusinessException(
                    "La cantidad a aumentar debe ser mayor que cero."
            );
        }

        int stockActual =
                item.getStock() != null
                        ? item.getStock()
                        : 0;

        item.setStock(
                stockActual + cantidad
        );

        item.setUltimaActualizacion(
                LocalDateTime.now()
        );

        inventarioRepository.save(item);
    }

    /**
     * Verifica si existe suficiente stock.
     */
    public void validarStock(
            Inventario producto,
            int cantidad) {

        if (producto == null) {
            throw new BusinessException(
                    "Producto no encontrado."
            );
        }

        if (cantidad <= 0) {
            throw new BusinessException(
                    "La cantidad debe ser mayor que cero."
            );
        }

        if (producto.getStock() == null) {
            throw new BusinessException(
                    "El producto no tiene stock disponible."
            );
        }

        if (producto.getStock() < cantidad) {
            throw new BusinessException(
                    "No hay suficiente stock de "
                            + producto.getNombre()
                            + ". Stock disponible: "
                            + producto.getStock()
            );
        }
    }

    /**
     * Comprueba si un producto puede venderse.
     */
    public void validarDisponibleParaVenta(
            Inventario producto) {

        if (producto == null) {
            throw new BusinessException(
                    "Producto no encontrado."
            );
        }

        if (producto.getEstado() != EstadoInventario.ACTIVO) {
            throw new BusinessException(
                    "El producto no está disponible para la venta."
            );
        }

        if (producto.getStock() == null ||
                producto.getStock() <= 0) {

            throw new BusinessException(
                    "El producto "
                            + producto.getNombre()
                            + " no tiene stock disponible."
            );
        }
    }

    /**
     * Validaciones generales del producto.
     */
    private void validarDatos(Inventario datos) {

        if (datos == null) {
            throw new BusinessException(
                    "Los datos del producto son obligatorios."
            );
        }

        if (datos.getNombre() == null ||
                datos.getNombre().isBlank()) {

            throw new BusinessException(
                    "El nombre del producto es obligatorio."
            );
        }

        if (datos.getStock() == null ||
                datos.getStock() < 0) {

            throw new BusinessException(
                    "El stock no puede ser negativo."
            );
        }

        if (datos.getPrecioUnitario() == null ||
                datos.getPrecioUnitario().signum() <= 0) {

            throw new BusinessException(
                    "El precio unitario debe ser mayor que cero."
            );
        }
    }
}