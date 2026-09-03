package com.wilsonmontenegro.odontologia.security;

import com.wilsonmontenegro.odontologia.model.Usuario;
import com.wilsonmontenegro.odontologia.model.enums.Rol;
import com.wilsonmontenegro.odontologia.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoogleOAuth2UserService
        implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    private final DefaultOAuth2UserService delegate =
            new DefaultOAuth2UserService();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {

        OAuth2User oauth2User = delegate.loadUser(userRequest);

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Google no proporcionó un correo electrónico");
        }

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseGet(() -> {

                    Usuario nuevoUsuario = Usuario.builder()
                            .name(name != null ? name : "Usuario Google")
                            .email(email)
                            .password(
                                    passwordEncoder.encode(
                                            UUID.randomUUID().toString()
                                    )
                            )
                            .rol(Rol.CLIENTE)
                            .build();

                    return usuarioRepository.save(nuevoUsuario);
                });

        return new GoogleOAuth2User(
                oauth2User,
                usuario
        );
    }
}