package com.wilsonmontenegro.odontologia.model.enums;

/**
 * Estados posibles de una cita.
 * Equivalente a: ENUM('pendiente','confirmada','cancelada','atendida') en Laravel.
 */
public enum EstadoCita {
    PENDIENTE,
    CONFIRMADA,
    CANCELADA,
    ATENDIDA
}
