package com.wilsonmontenegro.odontologia.service;

import com.wilsonmontenegro.odontologia.exception.BusinessException;
import com.wilsonmontenegro.odontologia.exception.RecursoNoEncontradoException;
import com.wilsonmontenegro.odontologia.model.Cita;
import com.wilsonmontenegro.odontologia.model.Cliente;
import com.wilsonmontenegro.odontologia.model.Servicio;
import com.wilsonmontenegro.odontologia.model.enums.EstadoCita;
import com.wilsonmontenegro.odontologia.repository.CitaRepository;
import com.wilsonmontenegro.odontologia.repository.ClienteRepository;
import com.wilsonmontenegro.odontologia.repository.ServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Logica de negocio de citas: agendar, editar, eliminar, con las mismas reglas
 * que existian repetidas en AdminCitaController / EmpleadoCitaController / ClienteCitaController.
 * <p>
 * Reglas replicadas 1:1 desde Laravel:
 * 1. La fecha de entrada no puede ser pasada.
 * 2. La cita debe estar dentro del horario laboral (06:00 - 20:00).
 * 3. No puede haber solapamiento con otra cita existente.
 * 4. La duracion de una cita es siempre de 1 hora (calcularFechaSalida).
 */
@Service
@RequiredArgsConstructor
public class CitaService {

    private final CitaRepository citaRepository;
    private final ClienteRepository clienteRepository;
    private final ServicioRepository servicioRepository;

    @Value("${app.horario.apertura}")
    private String horaApertura;

    @Value("${app.horario.cierre}")
    private String horaCierre;

    private static final DateTimeFormatter FORMATO_LEGIBLE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public List<Cita> listarTodas() {
        return citaRepository.findAllByOrderByFechaEntradaDesc();
    }

    public List<Cita> buscar(String texto) {
        if (texto == null || texto.isBlank()) {
            return listarTodas();
        }
        if (texto.trim().chars().allMatch(Character::isDigit)) {
            throw new BusinessException("Solo se puede buscar por nombre, estado o servicio.");
        }
        return citaRepository.buscar(texto.trim());
    }

    public List<Cita> listarPorUsuario(Long usuarioId) {
        return citaRepository.findByUsuarioIdOrderByFechaEntradaDesc(usuarioId);
    }

    public Cita obtenerPorId(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada"));
    }

    /** Agendar cita desde el panel de Administrador o Empleado (pueden fijar el estado). */
    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.SERIALIZABLE)
    public Cita agendar(LocalDateTime fechaEntrada, Long idServicio, Long idCliente, EstadoCita estado) {
        LocalDateTime fechaSalida = calcularFechaSalida(fechaEntrada);
        validarReglasDeAgenda(fechaEntrada, fechaSalida, null);

        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new BusinessException("Cliente no encontrado."));
        Servicio servicio = servicioRepository.findById(idServicio)
                .orElseThrow(() -> new BusinessException("Servicio no encontrado."));

        Cita cita = Cita.builder()
                .fechaEntrada(fechaEntrada)
                .fechaSalida(fechaSalida)
                .estado(estado != null ? estado : EstadoCita.PENDIENTE)
                .cliente(cliente)
                .servicio(servicio)
                .build();

        return citaRepository.save(cita);
    }

    /** Agendar cita desde el portal del Cliente: siempre queda en PENDIENTE. */
    @Transactional
    public Cita agendarComoCliente(LocalDateTime fechaEntrada, Long idServicio, Long usuarioId) {
        Cliente cliente = clienteRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new BusinessException("Cliente no encontrado."));

        return agendar(fechaEntrada, idServicio, cliente.getIdCliente(), EstadoCita.PENDIENTE);
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.SERIALIZABLE)
    public Cita actualizar(Long id, LocalDateTime fechaEntrada, Long idServicio, Long idCliente, EstadoCita estado) {
        Cita cita = obtenerPorId(id);
        LocalDateTime fechaSalida = calcularFechaSalida(fechaEntrada);
        validarReglasDeAgenda(fechaEntrada, fechaSalida, id);

        cita.setFechaEntrada(fechaEntrada);
        cita.setFechaSalida(fechaSalida);

        if (idServicio != null) {
            cita.setServicio(servicioRepository.findById(idServicio)
                    .orElseThrow(() -> new BusinessException("Servicio no encontrado.")));
        }
        if (idCliente != null) {
            cita.setCliente(clienteRepository.findById(idCliente)
                    .orElseThrow(() -> new BusinessException("Cliente no encontrado.")));
        }
        if (estado != null) {
            cita.setEstado(estado);
        }

        return citaRepository.save(cita);
    }

    /** Actualizar como cliente: no puede cambiar de estado (siempre vuelve a Pendiente). */
    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.SERIALIZABLE)
    public Cita actualizarComoCliente(Long id, LocalDateTime fechaEntrada, Long idServicio, Long usuarioId) {
        Cita cita = obtenerPorId(id);
        validarPropietario(cita, usuarioId);

        LocalDateTime fechaSalida = calcularFechaSalida(fechaEntrada);
        validarReglasDeAgenda(fechaEntrada, fechaSalida, id);

        cita.setFechaEntrada(fechaEntrada);
        cita.setFechaSalida(fechaSalida);
        cita.setEstado(EstadoCita.PENDIENTE);
        if (idServicio != null) {
            cita.setServicio(servicioRepository.findById(idServicio)
                    .orElseThrow(() -> new BusinessException("Servicio no encontrado.")));
        }

        return citaRepository.save(cita);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!citaRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Cita no encontrada");
        }
        citaRepository.deleteById(id);
    }

    public void validarPropietario(Cita cita, Long usuarioId) {
        if (cita.getCliente() == null || cita.getCliente().getUsuario() == null
                || !cita.getCliente().getUsuario().getId().equals(usuarioId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "No tienes permiso sobre esta cita.");
        }
    }

    // ── Reglas de negocio ────────────────────────────────────────────────

    private void validarReglasDeAgenda(LocalDateTime entrada, LocalDateTime salida, Long excluirId) {
        if (entrada.isBefore(LocalDateTime.now())) {
            throw new BusinessException("No es posible agendar una cita en una fecha pasada.");
        }

        LocalTime apertura = LocalTime.parse(horaApertura);
        LocalTime cierre = LocalTime.parse(horaCierre);
        LocalTime horaEntrada = entrada.toLocalTime();
        LocalTime horaSalida = salida.toLocalTime();

        if (horaEntrada.isBefore(apertura) || horaEntrada.isAfter(cierre)
                || horaSalida.isBefore(apertura) || horaSalida.isAfter(cierre)) {
            throw new BusinessException(
                    "Las citas solo pueden agendarse dentro del horario laboral (" + horaApertura + " a " + horaCierre + ").");
        }

        citaRepository.buscarSolapamiento(entrada, salida, excluirId).ifPresent(disponibleDesde -> {
            throw new BusinessException(
                    "Esta hora esta ocupada. Disponible desde: " + disponibleDesde.format(FORMATO_LEGIBLE));
        });
    }

    private LocalDateTime calcularFechaSalida(LocalDateTime entrada) {
        return entrada.plusHours(1);
    }
}
