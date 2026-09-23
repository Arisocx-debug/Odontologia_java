package com.wilsonmontenegro.odontologia.service;

import com.wilsonmontenegro.odontologia.exception.BusinessException;
import com.wilsonmontenegro.odontologia.exception.RecursoNoEncontradoException;
import com.wilsonmontenegro.odontologia.model.Proveedor;
import com.wilsonmontenegro.odontologia.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;

    public List<Proveedor> listarTodos() {
        return proveedorRepository.findAll();
    }

    /**
     * Busca proveedores por sus datos de contacto y fecha de registro.
     * Los criterios indicados se aplican simultaneamente.
     */
    public List<Proveedor> buscarConFiltros(String search, LocalDate fechaDesde, LocalDate fechaHasta) {
        String termino = search == null ? "" : search.trim().toLowerCase();

        return proveedorRepository.findAll().stream()
                .filter(proveedor -> termino.isBlank()
                        || contiene(proveedor.getNombre(), termino)
                        || contiene(proveedor.getContacto(), termino)
                        || contiene(proveedor.getTelefono(), termino)
                        || contiene(proveedor.getEmail(), termino)
                        || contiene(proveedor.getDireccion(), termino))
                .filter(proveedor -> fechaDesde == null || (proveedor.getCreatedAt() != null
                        && !proveedor.getCreatedAt().toLocalDate().isBefore(fechaDesde)))
                .filter(proveedor -> fechaHasta == null || (proveedor.getCreatedAt() != null
                        && !proveedor.getCreatedAt().toLocalDate().isAfter(fechaHasta)))
                .sorted(java.util.Comparator.comparing(Proveedor::getNombre, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private boolean contiene(String valor, String termino) {
        return valor != null && valor.toLowerCase().contains(termino);
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
