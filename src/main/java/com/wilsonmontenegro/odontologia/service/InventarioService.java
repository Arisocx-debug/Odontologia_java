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

    // Mostrar todos
    public List<Inventario> listarTodos() {
        return inventarioRepository.findAll();
    }

    // Buscar
    public List<Inventario> buscar(String texto) {
        if (texto == null || texto.isBlank()) {
            return listarTodos();
        }
        return inventarioRepository.buscar(texto.trim());
    }

    // Obtener por ID
    public Inventario obtenerPorId(Long id) {
        return inventarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto de inventario no encontrado"));
    }

    // Crear
    @Transactional
    public Inventario crear(Inventario datos) {
        validarDatos(datos);
        datos.setEstado(EstadoInventario.ACTIVO);
        datos.setUltimaActualizacion(LocalDateTime.now());
        return inventarioRepository.save(datos);
    }

    // Actualizar
    @Transactional
    public Inventario actualizar(Long id, Inventario datos) {
        Inventario item = obtenerPorId(id);
        validarDatos(datos);

        item.setNombre(datos.getNombre());
        item.setStock(datos.getStock());
        item.setPrecioUnitario(datos.getPrecioUnitario());
        item.setNombreProveedor(datos.getNombreProveedor());
        item.setDescripcion(datos.getDescripcion());
        item.setUltimaActualizacion(LocalDateTime.now());

        return inventarioRepository.save(item);
    }

    // Eliminar
    @Transactional
    public void eliminar(Long id) {
        inventarioRepository.deleteById(id);
    }

    // Activar / desactivar
    @Transactional
    public Inventario toggleEstado(Long id) {
        Inventario item = obtenerPorId(id);

        if (item.getEstado() == EstadoInventario.INACTIVO) {
            validarDatos(item);
        }

        item.setEstado(
                item.getEstado() == EstadoInventario.ACTIVO
                        ? EstadoInventario.INACTIVO
                        : EstadoInventario.ACTIVO
        );

        item.setUltimaActualizacion(LocalDateTime.now());
        return inventarioRepository.save(item);
    }

    // Descontar stock (uso interno)
    @Transactional
    public void descontarStock(Inventario item, int cantidad) {
        if (cantidad > item.getStock()) {
            throw new BusinessException("No hay suficiente stock de " + item.getNombre() + ".");
        }
        item.setStock(item.getStock() - cantidad);
        item.setUltimaActualizacion(LocalDateTime.now());
        inventarioRepository.save(item);
    }

    // Validación
    private void validarDatos(Inventario datos) {
        if (datos.getStock() == null || datos.getStock() < 0) {
            throw new BusinessException("El stock no puede ser negativo.");
        }
        if (datos.getPrecioUnitario() == null || datos.getPrecioUnitario().signum() <= 0) {
            throw new BusinessException("El precio unitario debe ser mayor que cero.");
        }
    }

    // 🔥 Método que usa el checkout
    @Transactional
    public void actualizarStock(Long idInventario, int cantidadVendida) {

        Inventario producto = obtenerPorId(idInventario);

        if (cantidadVendida > producto.getStock()) {
            throw new BusinessException("No hay suficiente stock de " + producto.getNombre());
        }

        // Reducir stock
        producto.setStock(producto.getStock() - cantidadVendida);

        // Si tu tabla tiene campo "ventas", lo actualizas aquí
        try {
            producto.setVentas(producto.getVentas() + cantidadVendida);
        } catch (Exception e) {
            // Si no existe el campo ventas, no pasa nada
        }

        producto.setUltimaActualizacion(LocalDateTime.now());
        inventarioRepository.save(producto);
    }
}
