package com.wilsonmontenegro.odontologia.controller;

import com.wilsonmontenegro.odontologia.dto.request.LoginRequest;
import com.wilsonmontenegro.odontologia.dto.request.RegistroRequest;
import com.wilsonmontenegro.odontologia.dto.response.AuthResponse;
import com.wilsonmontenegro.odontologia.exception.BusinessException;
import com.wilsonmontenegro.odontologia.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Controller;
import org.springframework.http.ResponseCookie;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de vistas para login/registro/logout. Equivalente a AuthController.php.
 * En vez de guardar datos en session(), genera un JWT y lo coloca en una cookie HttpOnly
 * para que la navegacion normal del navegador (sin JS) siga funcionando con Thymeleaf.
 */
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

    @GetMapping("/login")
    public String mostrarLogin() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
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
            model.addAttribute("error", "Credenciales invalidas");
            return "auth/login";
        }
    }

    @GetMapping("/register")
    public String mostrarRegistro() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String registrar(@RequestParam String nombre,
                             @RequestParam String apellido,
                             @RequestParam String email,
                             @RequestParam String telefono,
                             @RequestParam String password,
                             Model model) {
        try {
            RegistroRequest request = new RegistroRequest();
            request.setNombre(nombre);
            request.setApellido(apellido);
            request.setEmail(email);
            request.setTelefono(telefono);
            request.setPassword(password);

            authService.registrar(request);

            model.addAttribute("mensaje", "Registro exitoso, ahora puedes iniciar sesion.");
            return "auth/login";
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        response.addHeader("Set-Cookie", crearCookie("").maxAge(0).build().toString());
        return "redirect:/login";
    }

    private void agregarCookieToken(HttpServletResponse response, String token) {
        response.addHeader("Set-Cookie", crearCookie(token)
                .maxAge(java.time.Duration.ofMillis(expirationMs))
                .build()
                .toString());
    }

    private ResponseCookie.ResponseCookieBuilder crearCookie(String value) {
        return ResponseCookie.from(cookieName, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/");
    }
}
