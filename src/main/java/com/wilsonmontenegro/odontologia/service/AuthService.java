package com.wilsonmontenegro.odontologia.service;

import com.wilsonmontenegro.odontologia.dto.request.LoginRequest;
import com.wilsonmontenegro.odontologia.dto.request.RegistroRequest;
import com.wilsonmontenegro.odontologia.dto.response.AuthResponse;
import com.wilsonmontenegro.odontologia.exception.BusinessException;
import com.wilsonmontenegro.odontologia.model.Cliente;
import com.wilsonmontenegro.odontologia.model.Usuario;
import com.wilsonmontenegro.odontologia.model.enums.Rol;
import com.wilsonmontenegro.odontologia.repository.ClienteRepository;
import com.wilsonmontenegro.odontologia.repository.UsuarioRepository;
import com.wilsonmontenegro.odontologia.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Logica de autenticacion. Equivalente a AuthController.php.
 * En Laravel el login guardaba datos en session(); aqui se genera un JWT
 * y se calcula la URL de redireccion segun el rol, igual que el `match($users->rol)` original.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (Exception e) {
            throw new BadCredentialsException("Credenciales invalidas");
        }

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Credenciales invalidas"));

        String token = jwtTokenProvider.generarToken(
                usuario.getId(), usuario.getEmail(), usuario.getRol().name(), usuario.getName()
        );

        return AuthResponse.builder()
                .token(token)
                .id(usuario.getId())
                .nombre(usuario.getName())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .redirectUrl(redireccionSegunRol(usuario.getRol()))
                .build();
    }

    @Transactional
    public AuthResponse registrar(RegistroRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Ya existe una cuenta con ese correo.");
        }

        Usuario usuario = Usuario.builder()
                .name((request.getNombre() + " " + request.getApellido()).trim())
                .email(request.getEmail())
                .telefono(request.getTelefono())
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(Rol.CLIENTE)
                .build();

        usuario = usuarioRepository.save(usuario);

        Cliente cliente = Cliente.builder()
                .usuario(usuario)
                .build();
        clienteRepository.save(cliente);

        String token = jwtTokenProvider.generarToken(
                usuario.getId(), usuario.getEmail(), usuario.getRol().name(), usuario.getName()
        );

        return AuthResponse.builder()
                .token(token)
                .id(usuario.getId())
                .nombre(usuario.getName())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .redirectUrl(redireccionSegunRol(usuario.getRol()))
                .build();
    }

    /** Equivalente al match($users->rol) del AuthController original. */
    public String redireccionSegunRol(Rol rol) {
        return switch (rol) {
            case ADMINISTRADOR -> "/admin/citas";
            case EMPLEADO -> "/empleado/citas";
            case CLIENTE -> "/cliente/citas";
        };
    }
}
