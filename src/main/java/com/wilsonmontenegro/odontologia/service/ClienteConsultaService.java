package com.wilsonmontenegro.odontologia.service;

import com.wilsonmontenegro.odontologia.model.Cliente;
import com.wilsonmontenegro.odontologia.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Utilidad para listar clientes junto con su usuario asociado, usada por los
 * formularios de citas en los paneles de Administrador y Empleado
 * (equivalente al Cliente::with('usuario')->get() de Laravel).
 */
@Service
@RequiredArgsConstructor
public class ClienteConsultaService {

    private final ClienteRepository clienteRepository;

    public List<Cliente> listarClientesConUsuario() {
        return clienteRepository.findAll().stream()
                .filter(c -> c.getUsuario() != null)
                .sorted(Comparator.comparing(c -> c.getUsuario().getName()))
                .toList();
    }
}
