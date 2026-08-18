package com.wilsonmontenegro.odontologia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private long totalCitas;
    private long pendientes;
    private long atendidas;
    private long canceladas;
    private BigDecimal ingresosMes;
    private int anio;
    private Integer mes;
}
