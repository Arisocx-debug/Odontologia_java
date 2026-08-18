package com.wilsonmontenegro.odontologia.controller.api;

import com.wilsonmontenegro.odontologia.dto.request.LoginRequest;
import com.wilsonmontenegro.odontologia.dto.request.RegistroRequest;
import com.wilsonmontenegro.odontologia.dto.response.AuthResponse;
import com.wilsonmontenegro.odontologia.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * API REST de autenticacion. Pensada para un frontend desacoplado (Angular/React/etc.),
 * complementaria a los formularios Thymeleaf de AuthWebController.
 * Devuelve el JWT en el body para que el cliente lo guarde y lo use en el header
 * "Authorization: Bearer {token}" en las siguientes peticiones.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/registro")
    public ResponseEntity<AuthResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.ok(authService.registrar(request));
    }
}
