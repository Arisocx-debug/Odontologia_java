package com.wilsonmontenegro.odontologia.service;

import com.wilsonmontenegro.odontologia.exception.BusinessException;
import com.wilsonmontenegro.odontologia.repository.CitaRepository;
import com.wilsonmontenegro.odontologia.exception.RecursoNoEncontradoException;
import com.wilsonmontenegro.odontologia.model.Servicio;
import com.wilsonmontenegro.odontologia.repository.ServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicioService {

    private final ServicioRepository servicioRepository;
    private final CitaRepository citaRepository;

    public List<Servicio> listarTodos() {
        return servicioRepository.findAll();
    }

    /**
     * Busca servicios por texto y rango de costo. Los criterios se combinan,
     * por lo que todos los filtros diligenciados deben cumplirse.
     */
    public List<Servicio> buscarConFiltros(String search, BigDecimal costoMinimo, BigDecimal costoMaximo) {
        String termino = search == null ? "" : search.trim().toLowerCase();

        return servicioRepository.findAllByOrderByNombreAsc().stream()
                .filter(servicio -> termino.isBlank()
                        || contiene(servicio.getNombre(), termino)
                        || contiene(servicio.getDescripcion(), termino))
                .filter(servicio -> costoMinimo == null || servicio.getCosto().compareTo(costoMinimo) >= 0)
                .filter(servicio -> costoMaximo == null || servicio.getCosto().compareTo(costoMaximo) <= 0)
                .toList();
    }

    private boolean contiene(String valor, String termino) {
        return valor != null && valor.toLowerCase().contains(termino);
    }

    public Servicio obtenerPorId(Long id) {
        return servicioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio no encontrado"));
    }

    @Transactional
    public Servicio crear(String nombre, String descripcion, BigDecimal costo) {
        Servicio servicio = Servicio.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .costo(costo)
                .build();
        return servicioRepository.save(servicio);
    }

    @Transactional
    public Servicio actualizar(Long id, String nombre, String descripcion, BigDecimal costo) {
        Servicio servicio = obtenerPorId(id);
        servicio.setNombre(nombre);
        servicio.setDescripcion(descripcion);
        servicio.setCosto(costo);
        return servicioRepository.save(servicio);
    }

    @Transactional
    public void eliminar(Long id) {

        Servicio servicio = obtenerPorId(id);

        long cantidadCitas = citaRepository.countByServicioIdServicio(id);

        if (cantidadCitas > 0) {
            throw new BusinessException(
                    "No se puede eliminar el servicio porque tiene citas relacionadas.");
        }

        servicioRepository.delete(servicio);
    }
}
