package com.wilsonmontenegro.odontologia.security;

import com.wilsonmontenegro.odontologia.model.Usuario;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class GoogleOAuth2User implements OAuth2User {

    private final OAuth2User delegate;
    private final Usuario usuario;

    public GoogleOAuth2User(
            OAuth2User delegate,
            Usuario usuario) {

        this.delegate = delegate;
        this.usuario = usuario;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return List.of(
                new SimpleGrantedAuthority(
                        "ROLE_" + usuario.getRol().name()
                )
        );
    }

    @Override
    public String getName() {
        return usuario.getEmail();
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getEmail() {
        return usuario.getEmail();
    }

    public String getNombre() {
        return usuario.getName();
    }

    public String getRol() {
        return usuario.getRol().name();
    }
}