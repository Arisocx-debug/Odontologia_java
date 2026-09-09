package com.wilsonmontenegro.odontologia.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final SesionActivaPublicoInterceptor sesionActivaPublicoInterceptor;

    @Value("${app.cors.allowed-origins:http://localhost:8080}")
    private List<String> allowedOrigins;

    @Value("${app.uploads.productos-dir:uploads/productos}")
    private String productosDir;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sesionActivaPublicoInterceptor)
                .addPathPatterns(
                        "/",
                        "/mision",
                        "/vision",
                        "/objetivos",
                        "/servicios-publicos",
                        "/servicios/publicos",
                        "/login",
                        "/register"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        Path productosPath = Paths.get(productosDir)
                .toAbsolutePath()
                .normalize();

        registry.addResourceHandler("/uploads/productos/**")
                .addResourceLocations(productosPath.toUri().toString());
    }

    /**
     * Configuración CORS para el prefijo /api/**.
     */
    @Bean
    public CorsFilter corsFilter() {

        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "PATCH",
                        "OPTIONS"
                )
        );

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/api/**", config);

        return new CorsFilter(source);
    }
}