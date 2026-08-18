package com.wilsonmontenegro.odontologia.service;

import com.wilsonmontenegro.odontologia.exception.BusinessException;
import com.wilsonmontenegro.odontologia.exception.RecursoNoEncontradoException;
import com.wilsonmontenegro.odontologia.model.Proveedor;
import com.wilsonmontenegro.odontologia.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;

    public List<Proveedor> listarTodos() {
        return proveedorRepository.findAll();
    }

    public Proveedor obtenerPorId(Long id) {
        return proveedorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Proveedor no encontrado"));
    }

    @Transactional
    public Proveedor crear(Proveedor datos) {
        if (proveedorRepository.existsByNombreIgnoreCase(datos.getNombre())) {
            throw new BusinessException("Ya existe un proveedor con ese nombre.");
        }
        return proveedorRepository.save(datos);
    }

    @Transactional
    public Proveedor actualizar(Long id, Proveedor datos) {
        Proveedor proveedor = obtenerPorId(id);

        proveedorRepository.findByNombreIgnoreCase(datos.getNombre()).ifPresent(existente -> {
            if (!existente.getId().equals(id)) {
                throw new BusinessException("Ya existe otro proveedor con ese nombre.");
            }
        });

        proveedor.setNombre(datos.getNombre());
        proveedor.setContacto(datos.getContacto());
        proveedor.setTelefono(datos.getTelefono());
        proveedor.setEmail(datos.getEmail());
        proveedor.setDireccion(datos.getDireccion());
        return proveedorRepository.save(proveedor);
    }

    @Transactional
    public void eliminar(Long id) {
        Proveedor proveedor = obtenerPorId(id);
        proveedorRepository.delete(proveedor);
    }
}
