package com.wilsonmontenegro.odontologia.config;

import com.wilsonmontenegro.odontologia.model.Usuario;
import com.wilsonmontenegro.odontologia.service.AuthService;
import com.wilsonmontenegro.odontologia.util.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * Si ya hay sesion, atras/adelante hacia inicio, mision, vision o login
 * no debe mostrar el sitio publico ni "volver a entrar": redirige al panel.
 */
@Component
@RequiredArgsConstructor
public class SesionActivaPublicoInterceptor implements HandlerInterceptor {

    private static final Set<String> RUTAS_PUBLICAS = Set.of(
            "/",
            "/mision",
            "/vision",
            "/objetivos",
            "/servicios-publicos",
            "/servicios/publicos",
            "/login",
            "/register"
    );

    private final AuthService authService;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        Usuario usuario = AuthUtil.usuarioActual();
        if (usuario == null) {
            return true;
        }

        String path = request.getRequestURI();
        String context = request.getContextPath();
        if (context != null && !context.isEmpty() && path.startsWith(context)) {
            path = path.substring(context.length());
        }
        if (path.isEmpty()) {
            path = "/";
        }

        if (!RUTAS_PUBLICAS.contains(path)) {
            return true;
        }

        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.sendRedirect(context + authService.redireccionSegunRol(usuario.getRol()));
        return false;
    }
}
