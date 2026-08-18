package com.wilsonmontenegro.odontologia.model.enums;

/**
 * Roles del sistema. Equivalente a la columna ENUM('administrador','empleado','cliente')
 * de la tabla `users` en el proyecto Laravel original.
 */
public enum Rol {
    ADMINISTRADOR,
    EMPLEADO,
    CLIENTE
}
