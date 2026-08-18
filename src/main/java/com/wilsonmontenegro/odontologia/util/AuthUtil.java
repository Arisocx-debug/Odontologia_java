package com.wilsonmontenegro.odontologia.util;

import com.wilsonmontenegro.odontologia.model.Usuario;
import com.wilsonmontenegro.odontologia.security.UsuarioPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Helper estatico para obtener el usuario logueado, evitando repetir
 * `(UsuarioPrincipal) SecurityContextHolder...` en cada controlador.
 * Equivalente a Auth::user() / session('IDusuario') de Laravel.
 */
public final class AuthUtil {

    private AuthUtil() {
    }

    public static UsuarioPrincipal principalActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioPrincipal principal)) {
            return null;
        }
        return principal;
    }

    public static Usuario usuarioActual() {
        UsuarioPrincipal principal = principalActual();
        return principal != null ? principal.getUsuario() : null;
    }

    public static Long idUsuarioActual() {
        UsuarioPrincipal principal = principalActual();
        return principal != null ? principal.getId() : null;
    }
}
