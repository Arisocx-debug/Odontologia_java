package com.wilsonmontenegro.odontologia.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Que hacer cuando un usuario NO autenticado intenta acceder a un recurso protegido.
 * - Si la ruta es de API (/api/**): responde 401 en JSON.
 * - Si es una vista Thymeleaf: redirige a /login (igual que hacia el middleware de Laravel
 *   con `redirect()->route('login')`).
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {

        if (request.getRequestURI().startsWith("/api/")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"No autenticado. Inicia sesion para continuar.\"}");
        } else {
            response.sendRedirect("/login");
        }
    }
}
