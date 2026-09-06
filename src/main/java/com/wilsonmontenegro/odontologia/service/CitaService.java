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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 
 *  * Logica de negocio de citas: agendar, editar, eliminar, con las mismas
 * reglas
 *
 *  * que existian repetidas en AdminCitaController / EmpleadoCitaController /
 * 
 *  * ClienteCitaController.
 * 
 *  *
 * <p>
 * 
 *  * Reglas replicadas 1:1 desde Laravel:
 * 
 *  * 1. La fecha de entrada no puede ser pasada.
 * 
 *  * 2. La cita debe estar dentro del horario laboral (06:00 - 20:00).
 * 
 *  * 3. No puede haber solapamiento con otra cita existente.
 * 
 *  * 4. La duracion de una cita es siempre de 1 hora (calcularFechaSalida).
 * 
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

    // ============================================================
    // CONSULTAS
    // ============================================================

    public List<Cita> listarTodas() {
        return citaRepository.findAllByOrderByFechaEntradaDesc();
    }

    /**
     * Citas activas para administrador y empleado.
     *
     * PENDIENTE y CONFIRMADA permanecen visibles.
     * ATENDIDA y CANCELADA permanecen visibles durante 2 días.
     */
    @Transactional(readOnly = true)
    public List<Cita> listarCitasActivas() {

        LocalDateTime limite = LocalDateTime.now().minusDays(2);

        return citaRepository.findCitasActivas(limite);
    }

    /**
     * Citas activas de un cliente.
     *
     * IMPORTANTE:
     * El usuarioId se utiliza directamente en la consulta del Repository,
     * por lo que solo se recuperan las citas pertenecientes a ese usuario.
     */
    @Transactional(readOnly = true)
    public List<Cita> listarCitasActivasPorUsuario(Long usuarioId) {

        LocalDateTime limite = LocalDateTime.now().minusDays(2);

        return citaRepository.findCitasActivasPorUsuario(
                usuarioId,
                limite);
    }

    /**
     * Historial general para administrador/empleado.
     */
    @Transactional(readOnly = true)
    public List<Cita> listarHistorial() {

        LocalDateTime limite = LocalDateTime.now().minusDays(2);

        return citaRepository.findHistorial(limite);
    }

    /**
     * Historial de un cliente.
     */
    @Transactional(readOnly = true)
    public List<Cita> listarHistorialPorUsuario(Long usuarioId) {

        LocalDateTime limite = LocalDateTime.now().minusDays(2);

        return citaRepository.findHistorialPorUsuario(
                usuarioId,
                limite);
    }

    /**
     * Historial general con filtros.
     */
    @Transactional(readOnly = true)
    public List<Cita> buscarHistorial(
            Long clienteId,
            String fechaDesde,
            String fechaHasta,
            EstadoCita estado,
            String busqueda) {

        LocalDateTime limite = LocalDateTime.now().minusDays(2);

        LocalDateTime desde = null;
        LocalDateTime hasta = null;

        if (fechaDesde != null && !fechaDesde.isBlank()) {
            desde = LocalDate
                    .parse(fechaDesde)
                    .atStartOfDay();
        }

        if (fechaHasta != null && !fechaHasta.isBlank()) {
            hasta = LocalDate
                    .parse(fechaHasta)
                    .atTime(23, 59, 59);
        }

        if (busqueda == null) {
            busqueda = "";
        }

        return citaRepository.buscarHistorial(
                limite,
                clienteId,
                desde,
                hasta,
                estado,
                busqueda.trim());
    }

    /**
     * Historial de un cliente con filtros.
     *
     * La consulta del Repository filtra por usuarioId directamente
     * en la base de datos.
     */
    @Transactional(readOnly = true)
    public List<Cita> buscarHistorialPorUsuario(
            Long usuarioId,
            String fechaDesde,
            String fechaHasta,
            EstadoCita estado,
            String busqueda) {

        LocalDateTime limite = LocalDateTime.now().minusDays(2);

        LocalDateTime desde = null;
        LocalDateTime hasta = null;

        if (fechaDesde != null && !fechaDesde.isBlank()) {
            desde = LocalDate
                    .parse(fechaDesde)
                    .atStartOfDay();
        }

        if (fechaHasta != null && !fechaHasta.isBlank()) {
            hasta = LocalDate
                    .parse(fechaHasta)
                    .atTime(23, 59, 59);
        }

        if (busqueda == null) {
            busqueda = "";
        }

        return citaRepository.buscarHistorialPorUsuario(
                usuarioId,
                limite,
                desde,
                hasta,
                estado,
                busqueda.trim());
    }

    /**
     * Búsqueda general para administrador/empleado.
     */
    public List<Cita> buscar(String texto) {

        if (texto == null || texto.isBlank()) {
            return listarTodas();
        }

        if (texto.trim().chars().allMatch(Character::isDigit)) {
            throw new BusinessException(
                    "Solo se puede buscar por nombre, estado o servicio.");
        }

        return citaRepository.buscar(texto.trim());
    }

    /**
     * Todas las citas pertenecientes a un usuario.
     */
    @Transactional(readOnly = true)
    public List<Cita> listarPorUsuario(Long usuarioId) {
        return citaRepository.findByUsuarioIdOrderByFechaEntradaDesc(usuarioId);
    }

    /**
     * Obtiene una cita junto con sus relaciones.
     */
    @Transactional(readOnly = true)
    public Cita obtenerPorId(Long id) {

        return citaRepository.findByIdConRelaciones(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Cita no encontrada"));
    }

    // ============================================================
    // CREAR CITA
    // ============================================================

    /**
     * Agendar cita desde administrador o empleado.
     *
     * Estos usuarios pueden definir el estado.
     */
    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.SERIALIZABLE)
    public Cita agendar(
            LocalDateTime fechaEntrada,
            Long idServicio,
            Long idCliente,
            EstadoCita estado) {

        LocalDateTime fechaSalida = calcularFechaSalida(fechaEntrada);

        validarReglasDeAgenda(
                fechaEntrada,
                fechaSalida,
                null);

        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new BusinessException(
                        "Cliente no encontrado."));

        Servicio servicio = servicioRepository.findById(idServicio)
                .orElseThrow(() -> new BusinessException(
                        "Servicio no encontrado."));

        Cita cita = Cita.builder()
                .fechaEntrada(fechaEntrada)
                .fechaSalida(fechaSalida)
                .estado(
                        estado != null
                                ? estado
                                : EstadoCita.PENDIENTE)
                .cliente(cliente)
                .servicio(servicio)
                .build();

        return citaRepository.save(cita);
    }

    /**
     * Agendar cita desde el portal del cliente.
     *
     * El cliente nunca puede escoger el estado.
     * Siempre comienza como PENDIENTE.
     */
    @Transactional
    public Cita agendarComoCliente(
            LocalDateTime fechaEntrada,
            Long idServicio,
            Long usuarioId) {

        Cliente cliente = clienteRepository
                .findByUsuarioId(usuarioId)
                .orElseThrow(() -> new BusinessException(
                        "Cliente no encontrado."));

        return agendar(
                fechaEntrada,
                idServicio,
                cliente.getIdCliente(),
                EstadoCita.PENDIENTE);
    }

    // ============================================================
    // ACTUALIZAR CITA
    // ============================================================

    /**
     * Actualización desde administrador/empleado.
     */
    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.SERIALIZABLE)
    public Cita actualizar(
            Long id,
            LocalDateTime fechaEntrada,
            Long idServicio,
            Long idCliente,
            EstadoCita estado) {

        Cita cita = obtenerPorId(id);

        LocalDateTime fechaSalida = calcularFechaSalida(fechaEntrada);

        validarReglasDeAgenda(
                fechaEntrada,
                fechaSalida,
                id);

        cita.setFechaEntrada(fechaEntrada);
        cita.setFechaSalida(fechaSalida);

        if (idServicio != null) {
            cita.setServicio(
                    servicioRepository.findById(idServicio)
                            .orElseThrow(() -> new BusinessException(
                                    "Servicio no encontrado.")));
        }

        if (idCliente != null) {
            cita.setCliente(
                    clienteRepository.findById(idCliente)
                            .orElseThrow(() -> new BusinessException(
                                    "Cliente no encontrado.")));
        }

        if (estado != null) {
            cita.setEstado(estado);
        }

        return citaRepository.save(cita);
    }

    /**
     * Actualizar cita desde el portal del cliente.
     *
     * El cliente solo puede modificar SUS PROPIAS citas.
     * El estado vuelve a PENDIENTE.
     */
    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.SERIALIZABLE)
    public Cita actualizarComoCliente(
            Long id,
            LocalDateTime fechaEntrada,
            Long idServicio,
            Long usuarioId) {

        Cita cita = obtenerPorId(id);

        // SEGURIDAD:
        // Comprobar que la cita pertenece al usuario autenticado.
        validarPropietario(cita, usuarioId);

        LocalDateTime fechaSalida = calcularFechaSalida(fechaEntrada);

        validarReglasDeAgenda(
                fechaEntrada,
                fechaSalida,
                id);

        cita.setFechaEntrada(fechaEntrada);
        cita.setFechaSalida(fechaSalida);

        // Un cliente no puede cambiar el estado.
        cita.setEstado(EstadoCita.PENDIENTE);

        if (idServicio != null) {
            cita.setServicio(
                    servicioRepository.findById(idServicio)
                            .orElseThrow(() -> new BusinessException(
                                    "Servicio no encontrado.")));
        }

        return citaRepository.save(cita);
    }

    // ============================================================
    // ELIMINAR
    // ============================================================

    @Transactional
    public void eliminar(Long id) {

        if (!citaRepository.existsById(id)) {
            throw new RecursoNoEncontradoException(
                    "Cita no encontrada");
        }

        citaRepository.deleteById(id);
    }

    // ============================================================
    // CANCELAR COMO CLIENTE
    // ============================================================

    /**
     * Cancela una cita desde el portal del cliente.
     *
     * IMPORTANTE:
     * La validación del propietario ahora está dentro del Service.
     * De esta manera ningún controller puede olvidar hacerla.
     */
    @Transactional
    public void cancelarComoCliente(
            Long id,
            Long usuarioId) {

        Cita cita = obtenerPorId(id);

        // SEGURIDAD:
        // El cliente solo puede cancelar SUS PROPIAS citas.
        validarPropietario(cita, usuarioId);

        // PENDIENTE:
        // Se puede cancelar sin restricción de tiempo.
        if (cita.getEstado() == EstadoCita.PENDIENTE) {

            cita.setEstado(EstadoCita.CANCELADA);
            citaRepository.save(cita);
            return;
        }

        // CONFIRMADA:
        // Solo se puede cancelar hasta 2 horas antes.
        if (cita.getEstado() == EstadoCita.CONFIRMADA) {

            LocalDateTime limiteCancelacion = cita.getFechaEntrada().minusHours(2);

            if (LocalDateTime.now()
                    .isAfter(limiteCancelacion)) {

                throw new BusinessException(
                        "Solo se puede cancelar una cita hasta 2 horas antes de la cita, cualquier duda contacte al 318 5377946.");
            }

            cita.setEstado(EstadoCita.CANCELADA);
            citaRepository.save(cita);
            return;
        }

        // ATENDIDA o CANCELADA.
        throw new BusinessException(
                "Esta cita no se puede cancelar.");
    }

    // ============================================================
    // SEGURIDAD
    // ============================================================

    /**
     * Comprueba que una cita pertenece al usuario autenticado.
     */
    public void validarPropietario(
            Cita cita,
            Long usuarioId) {

        if (usuarioId == null) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Usuario no autenticado.");
        }

        if (cita == null
                || cita.getCliente() == null
                || cita.getCliente().getUsuario() == null
                || cita.getCliente().getUsuario().getId() == null
                || !cita.getCliente()
                        .getUsuario()
                        .getId()
                        .equals(usuarioId)) {

            throw new org.springframework.security.access.AccessDeniedException(
                    "No tienes permiso sobre esta cita.");
        }
    }

    // ============================================================
    // REGLAS DE NEGOCIO
    // ============================================================

    private void validarReglasDeAgenda(
            LocalDateTime entrada,
            LocalDateTime salida,
            Long excluirId) {

        if (entrada.isBefore(LocalDateTime.now())) {
            throw new BusinessException(
                    "No es posible agendar una cita en una fecha pasada.");
        }

        LocalTime apertura = LocalTime.parse(horaApertura);

        LocalTime cierre = LocalTime.parse(horaCierre);

        LocalTime horaEntrada = entrada.toLocalTime();

        LocalTime horaSalida = salida.toLocalTime();

        if (horaEntrada.isBefore(apertura)
                || horaEntrada.isAfter(cierre)
                || horaSalida.isBefore(apertura)
                || horaSalida.isAfter(cierre)) {

            throw new BusinessException(
                    "Las citas solo pueden agendarse dentro del horario laboral ("
                            + horaApertura
                            + " a "
                            + horaCierre
                            + ").");
        }

        citaRepository
                .buscarSolapamiento(
                        entrada,
                        salida,
                        excluirId)
                .ifPresent(disponibleDesde -> {

                    throw new BusinessException(
                            "Esta hora esta ocupada. Disponible desde: "
                                    + disponibleDesde.format(
                                            FORMATO_LEGIBLE));
                });
    }

    private LocalDateTime calcularFechaSalida(
            LocalDateTime entrada) {

        return entrada.plusHours(1);
    }

}
