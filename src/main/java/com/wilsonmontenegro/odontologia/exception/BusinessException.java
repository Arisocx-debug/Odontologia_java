package com.wilsonmontenegro.odontologia.exception;

/**
 * Excepcion para reglas de negocio violadas (fecha pasada, fuera de horario,
 * solapamiento de citas, stock insuficiente, etc). Equivalente a los
 * `return redirect()->...->with('error', '...')` de los controladores Laravel.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
