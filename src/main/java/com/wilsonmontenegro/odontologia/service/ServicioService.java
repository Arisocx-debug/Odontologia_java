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
