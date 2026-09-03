package com.wilsonmontenegro.odontologia.controller;

import com.wilsonmontenegro.odontologia.dto.request.LoginRequest;
import com.wilsonmontenegro.odontologia.dto.request.RegistroRequest;
import com.wilsonmontenegro.odontologia.dto.response.AuthResponse;
import com.wilsonmontenegro.odontologia.exception.BusinessException;
import com.wilsonmontenegro.odontologia.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthWebController {

    private final AuthService authService;

    @Value("${app.jwt.cookie-name}")
    private String cookieName;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    @Value("${app.jwt.cookie-secure}")
    private boolean cookieSecure;

    @Value("${app.jwt.cookie-same-site}")
    private String cookieSameSite;

    // =========================
    // LOGIN
    // =========================

    @GetMapping("/login")
    public String mostrarLogin() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String email,
            @RequestParam String password,
            Model model,
            HttpServletResponse response) {

        try {

            LoginRequest request = new LoginRequest();

            request.setEmail(email);
            request.setPassword(password);

            AuthResponse auth = authService.login(request);

            agregarCookieToken(response, auth.getToken());

            return "redirect:" + auth.getRedirectUrl();

        } catch (BadCredentialsException e) {

            model.addAttribute(
                    "error",
                    "Credenciales invalidas"
            );

            return "auth/login";
        }
    }

    // =========================
    // REGISTRO - MOSTRAR FORMULARIO
    // =========================

    @GetMapping("/register")
    public String mostrarRegistro(Model model) {

        model.addAttribute(
                "registro",
                new RegistroRequest()
        );

        return "auth/register";
    }

    // =========================
    // REGISTRO - PROCESAR
    // =========================

    @PostMapping("/register")
    public String registrar(
            @Valid @ModelAttribute("registro") RegistroRequest request,
            BindingResult result,
            Model model) {

        // --------------------------------
        // 1. VALIDAR DATOS DEL FORMULARIO
        // --------------------------------

        if (result.hasErrors()) {

            if (result.hasFieldErrors("email")) {

                model.addAttribute(
                        "emailError",
                        "📧 Ingresa un correo electrónico válido para mantener segura tu cuenta."
                );
            }

            return "auth/register";
        }

        // --------------------------------
        // 2. REGISTRAR USUARIO
        // --------------------------------

        try {

            authService.registrar(request);

            // --------------------------------
            // 3. REGISTRO EXITOSO
            // --------------------------------

            model.addAttribute(
                    "mensaje",
                    "✅ ¡Registro exitoso! Tu cuenta ha sido creada correctamente."
            );

            return "auth/login";

        } catch (BusinessException e) {

            // --------------------------------
            // 4. CORREO YA REGISTRADO
            // --------------------------------

            model.addAttribute(
                    "error",
                    "⚠️ Este correo ya está registrado. Si ya tienes una cuenta, inicia sesión."
            );

            return "auth/register";
        }
    }

    // =========================
    // LOGOUT
    // =========================

    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        response.addHeader(
                HttpHeaders.SET_COOKIE,
                crearCookie("")
                        .maxAge(0)
                        .build()
                        .toString()
        );

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        return "redirect:/login";
    }

    // =========================
    // COOKIE JWT
    // =========================

    private void agregarCookieToken(
            HttpServletResponse response,
            String token) {

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                crearCookie(token)
                        .maxAge(
                                java.time.Duration.ofMillis(expirationMs)
                        )
                        .build()
                        .toString()
        );
    }

    private ResponseCookie.ResponseCookieBuilder crearCookie(
            String value) {

        return ResponseCookie
                .from(cookieName, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/");
    }
}