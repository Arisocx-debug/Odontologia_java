package com.wilsonmontenegro.odontologia.security;

import com.wilsonmontenegro.odontologia.model.Cliente;
import com.wilsonmontenegro.odontologia.model.Usuario;
import com.wilsonmontenegro.odontologia.model.enums.Rol;
import com.wilsonmontenegro.odontologia.repository.ClienteRepository;
import com.wilsonmontenegro.odontologia.repository.UsuarioRepository;
import com.wilsonmontenegro.odontologia.service.EmailService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoogleOAuth2UserService
                implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

        private final UsuarioRepository usuarioRepository;
        private final ClienteRepository clienteRepository;
        private final PasswordEncoder passwordEncoder;
        private final EmailService emailService;

        private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

        @Override
        @Transactional
        public OAuth2User loadUser(OAuth2UserRequest userRequest) {

                OAuth2User oauth2User = delegate.loadUser(userRequest);

                String email = oauth2User.getAttribute("email");
                String name = oauth2User.getAttribute("name");

                if (email == null || email.isBlank()) {
                        throw new IllegalArgumentException(
                                        "Google no proporcionó un correo electrónico");
                }

                // Buscar usuario existente o crear uno nuevo
                Usuario usuario = usuarioRepository.findByEmail(email)
                                .orElseGet(() -> {

                                        Usuario nuevoUsuario = Usuario.builder()
                                                        .name(name != null ? name : "Usuario Google")
                                                        .email(email)
                                                        .password(
                                                                        passwordEncoder.encode(
                                                                                        UUID.randomUUID().toString()))
                                                        .rol(Rol.CLIENTE)
                                                        .build();

                                        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

                                        // Crear también el perfil de Cliente
                                        Cliente cliente = Cliente.builder()
                                                        .usuario(usuarioGuardado)
                                                        .build();

                                        clienteRepository.save(cliente);

                                        // Correo de bienvenida solo para cuentas nuevas
                                        emailService.enviarBienvenida(
                                                        usuarioGuardado.getEmail(),
                                                        usuarioGuardado.getName());

                                        return usuarioGuardado;
                                });

                // Si el usuario ya existía pero no tenía Cliente,
                // se crea el registro faltante.
                if (usuario.getRol() == Rol.CLIENTE
                                && !clienteRepository.existsByUsuarioId(usuario.getId())) {

                        Cliente cliente = Cliente.builder()
                                        .usuario(usuario)
                                        .build();

                        clienteRepository.save(cliente);
                }

                return new GoogleOAuth2User(
                                oauth2User,
                                usuario);
        }
}