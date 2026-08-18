package com.wilsonmontenegro.odontologia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicacion.
 * <p>
 * Sistema de Gestion Odontologica - Dr. Wilson Montenegro.
 * Migrado desde Laravel/PHP a Spring Boot siguiendo una arquitectura en capas:
 * controller -> service -> repository -> entity (JPA).
 */
@SpringBootApplication
public class OdontologiaApplication {

    public static void main(String[] args) {
        SpringApplication.run(OdontologiaApplication.class, args);
    }
}
