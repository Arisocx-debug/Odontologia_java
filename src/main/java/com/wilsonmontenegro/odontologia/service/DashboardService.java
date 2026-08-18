package com.wilsonmontenegro.odontologia.service;

import com.wilsonmontenegro.odontologia.dto.response.DashboardResponse;
import com.wilsonmontenegro.odontologia.model.enums.EstadoCita;
import com.wilsonmontenegro.odontologia.repository.CitaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;

/**
 * Estadisticas del panel principal. Equivalente a DashboardController.php.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CitaRepository citaRepository;

    public DashboardResponse obtenerEstadisticas(Integer anio, Integer mes) {
        int anioFinal = anio != null ? anio : Year.now().getValue();
        Integer mesFinal = (mes != null && mes >= 1 && mes <= 12) ? mes : null;

        LocalDateTime desde;
        LocalDateTime hasta;

        if (mesFinal != null) {
            desde = LocalDateTime.of(anioFinal, mesFinal, 1, 0, 0);
            hasta = desde.plusMonths(1).minusSeconds(1);
        } else {
            desde = LocalDateTime.of(anioFinal, 1, 1, 0, 0);
            hasta = LocalDateTime.of(anioFinal, 12, 31, 23, 59, 59);
        }

        long total = citaRepository.countByFechaEntradaBetween(desde, hasta);
        long pendientes = citaRepository.countByFechaEntradaBetweenAndEstado(desde, hasta, EstadoCita.PENDIENTE);
        long atendidas = citaRepository.countByFechaEntradaBetweenAndEstado(desde, hasta, EstadoCita.ATENDIDA);
        long canceladas = citaRepository.countByFechaEntradaBetweenAndEstado(desde, hasta, EstadoCita.CANCELADA);
        BigDecimal ingresos = citaRepository.sumIngresosEntreFechas(desde, hasta);

        return DashboardResponse.builder()
                .totalCitas(total)
                .pendientes(pendientes)
                .atendidas(atendidas)
                .canceladas(canceladas)
                .ingresosMes(ingresos != null ? ingresos : BigDecimal.ZERO)
                .anio(anioFinal)
                .mes(mesFinal)
                .build();
    }
}
