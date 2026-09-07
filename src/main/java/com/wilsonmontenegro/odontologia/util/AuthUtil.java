package com.wilsonmontenegro.odontologia.util;

import com.wilsonmontenegro.odontologia.model.Usuario;
import com.wilsonmontenegro.odontologia.security.GoogleOAuth2User;
import com.wilsonmontenegro.odontologia.security.UsuarioPrincipal;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Helper estático para obtener el usuario logueado.
 *
 * Compatible con:
 * - Login normal mediante UsuarioPrincipal
 * - Login con Google mediante GoogleOAuth2User
 */
public final class AuthUtil {

    private AuthUtil() {
    }

    public static UsuarioPrincipal principalActual() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !(authentication.getPrincipal() instanceof UsuarioPrincipal principal)) {
            return null;
        }

        return principal;
    }

    public static Usuario usuarioActual() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        // Login normal
        if (principal instanceof UsuarioPrincipal usuarioPrincipal) {
            return usuarioPrincipal.getUsuario();
        }

        // Login con Google
        if (principal instanceof GoogleOAuth2User googleUser) {
            return googleUser.getUsuario();
        }

        return null;
    }

    public static Long idUsuarioActual() {

        Usuario usuario = usuarioActual();

        return usuario != null ? usuario.getId() : null;
    }
}