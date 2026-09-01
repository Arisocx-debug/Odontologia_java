package com.wilsonmontenegro.odontologia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

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

    /** Miles con punto: 1000000.00 → 1.000.000.00 */
    public String getIngresosMesFormateado() {
        BigDecimal valor = ingresosMes == null ? BigDecimal.ZERO : ingresosMes;
        valor = valor.setScale(2, RoundingMode.HALF_UP);
        boolean negativo = valor.signum() < 0;
        valor = valor.abs();

        String texto = valor.toPlainString();
        int puntoDecimal = texto.indexOf('.');
        String enteros = puntoDecimal >= 0 ? texto.substring(0, puntoDecimal) : texto;
        String decimales = puntoDecimal >= 0 ? texto.substring(puntoDecimal + 1) : "00";

        StringBuilder miles = new StringBuilder();
        int largo = enteros.length();
        for (int i = 0; i < largo; i++) {
            if (i > 0 && (largo - i) % 3 == 0) {
                miles.append('.');
            }
            miles.append(enteros.charAt(i));
        }
        return (negativo ? "-" : "") + miles + "." + decimales;
    }
}
