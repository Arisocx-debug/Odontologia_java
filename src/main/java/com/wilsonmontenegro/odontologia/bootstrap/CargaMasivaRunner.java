package com.wilsonmontenegro.odontologia.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Arranca la carga masiva al levantar Spring Boot, si app.carga-masiva.enabled=true.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.carga-masiva.enabled", havingValue = "true")
public class CargaMasivaRunner implements ApplicationRunner {

    private final CargaMasivaService cargaMasivaService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Iniciando carga masiva de datos de demostracion...");
        cargaMasivaService.ejecutarSiBaseVacia();
    }
}
