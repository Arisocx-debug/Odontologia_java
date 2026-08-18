package com.wilsonmontenegro.odontologia.service;

import com.wilsonmontenegro.odontologia.exception.BusinessException;
import com.wilsonmontenegro.odontologia.exception.RecursoNoEncontradoException;
import com.wilsonmontenegro.odontologia.model.Inventario;
import com.wilsonmontenegro.odontologia.model.enums.EstadoInventario;
import com.wilsonmontenegro.odontologia.repository.InventarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Logica de negocio de Inventario. Equivalente a InventarioController.php.
 * Los items inactivos no se pueden editar, actualizar ni eliminar
 * (misma regla que el `if ($item->estado === 'inactivo')` original).
 */
@Service
@RequiredArgsConstructor
public class InventarioService {

    private final InventarioRepository inventarioRepository;

    public List<Inventario> listarTodos() {
        return inventarioRepository.findAll();
    }

    public List<Inventario> buscar(String texto) {
        if (texto == null || texto.isBlank()) {
            return listarTodos();
        }
        return inventarioRepository.buscar(texto.trim());
    }

    public Inventario obtenerPorId(Long id) {
        return inventarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto de inventario no encontrado"));
    }

    @Transactional
    public Inventario crear(Inventario datos) {
        validarDatos(datos);
        datos.setEstado(EstadoInventario.ACTIVO);
        datos.setUltimaActualizacion(LocalDateTime.now());
        return inventarioRepository.save(datos);
    }

    @Transactional
    public Inventario actualizar(Long id, Inventario datos) {
        Inventario item = obtenerPorId(id);
        validarActivo(item, "actualizado");
        validarDatos(datos);

        item.setNombre(datos.getNombre());
        item.setStock(datos.getStock());
        item.setPrecioUnitario(datos.getPrecioUnitario());
        item.setNombreProveedor(datos.getNombreProveedor());
        item.setDescripcion(datos.getDescripcion());
        item.setUltimaActualizacion(LocalDateTime.now());

        return inventarioRepository.save(item);
    }

    @Transactional
    public void eliminar(Long id) {
        Inventario item = obtenerPorId(id);
        validarActivo(item, "eliminado");
        inventarioRepository.delete(item);
    }

    @Transactional
    public Inventario toggleEstado(Long id) {
        Inventario item = obtenerPorId(id);
        item.setEstado(item.getEstado() == EstadoInventario.ACTIVO
                ? EstadoInventario.INACTIVO
                : EstadoInventario.ACTIVO);
        return inventarioRepository.save(item);
    }

    /** Descuenta stock (usado por ventas). Lanza excepcion si no hay suficiente. */
    @Transactional
    public void descontarStock(Inventario item, int cantidad) {
        if (cantidad > item.getStock()) {
            throw new BusinessException("No hay suficiente stock de " + item.getNombre() + ".");
        }
        item.setStock(item.getStock() - cantidad);
        item.setUltimaActualizacion(LocalDateTime.now());
        inventarioRepository.save(item);
    }

    private void validarActivo(Inventario item, String accion) {
        if (item.getEstado() == EstadoInventario.INACTIVO) {
            throw new BusinessException("Este producto esta deshabilitado y no puede ser " + accion + ".");
        }
    }

    private void validarDatos(Inventario datos) {
        if (datos.getStock() == null || datos.getStock() < 0) {
            throw new BusinessException("El stock no puede ser negativo.");
        }
        if (datos.getPrecioUnitario() == null || datos.getPrecioUnitario().signum() <= 0) {
            throw new BusinessException("El precio unitario debe ser mayor que cero.");
        }
    }
}
