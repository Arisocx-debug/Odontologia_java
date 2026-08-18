package com.wilsonmontenegro.odontologia.service;

import com.wilsonmontenegro.odontologia.exception.BusinessException;
import com.wilsonmontenegro.odontologia.exception.RecursoNoEncontradoException;
import com.wilsonmontenegro.odontologia.model.Cliente;
import com.wilsonmontenegro.odontologia.model.Usuario;
import com.wilsonmontenegro.odontologia.model.enums.Rol;
import com.wilsonmontenegro.odontologia.repository.ClienteRepository;
import com.wilsonmontenegro.odontologia.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gestion de usuarios desde el panel de administrador. Equivalente a AdminUsuarioController.php.
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public List<Usuario> buscar(String texto) {
        if (texto == null || texto.isBlank()) {
            return usuarioRepository.findAll();
        }
        return usuarioRepository.buscar(texto.trim());
    }

    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    @Transactional
    public Usuario crear(String nombre, String email, String telefono, Rol rol, String password) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new BusinessException("Ya existe una cuenta con ese correo.");
        }

        Usuario usuario = Usuario.builder()
                .name(nombre)
                .email(email)
                .telefono(telefono)
                .rol(rol)
                .password(passwordEncoder.encode(password))
                .build();
        usuario = usuarioRepository.save(usuario);

        if (rol == Rol.CLIENTE) {
            Cliente cliente = Cliente.builder().usuario(usuario).build();
            clienteRepository.save(cliente);
        }

        return usuario;
    }

    @Transactional
    public Usuario actualizar(Long id, String nombre, String email, String telefono, Rol rol, String nuevaPassword) {
        Usuario usuario = obtenerPorId(id);

        usuarioRepository.findByEmail(email).ifPresent(existente -> {
            if (!existente.getId().equals(id)) {
                throw new BusinessException("Ya existe otra cuenta con ese correo.");
            }
        });

        usuario.setName(nombre);
        usuario.setEmail(email);
        usuario.setTelefono(telefono);
        usuario.setRol(rol);

        if (nuevaPassword != null && !nuevaPassword.isBlank()) {
            usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        }

        usuario = usuarioRepository.save(usuario);

        if (rol == Rol.CLIENTE && !clienteRepository.existsByUsuarioId(id)) {
            Cliente cliente = Cliente.builder().usuario(usuario).build();
            clienteRepository.save(cliente);
        }

        return usuario;
    }

    @Transactional
    public void eliminar(Long id) {
        Usuario usuario = obtenerPorId(id);

        if (usuario.getRol() == Rol.ADMINISTRADOR && usuarioRepository.countByRol(Rol.ADMINISTRADOR) <= 1) {
            throw new BusinessException("No se puede eliminar el unico administrador del sistema.");
        }

        usuarioRepository.delete(usuario);
    }
}
