package com.wilsonmontenegro.odontologia.config;

import com.wilsonmontenegro.odontologia.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * Configuracion central de seguridad.
 * <p>
 * Replica las reglas de los middlewares originales de Laravel:
 * - AdminMiddleware -> requiere rol ADMINISTRADOR (prefijo /admin/**)
 * - EmpleadoMiddleware -> requiere rol EMPLEADO (prefijo /empleado/**)
 * - ClienteMiddleware -> requiere rol CLIENTE (prefijo /cliente/**)
 * <p>
 * La autenticacion es stateless (JWT), pero el token tambien se guarda en una
 * cookie
 * HttpOnly para que las vistas Thymeleaf funcionen como una navegacion normal
 * de navegador,
 * sin tener que manejar el header Authorization manualmente desde el HTML.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService usuarioDetailsService;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final AccessDeniedHandler accessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(usuarioDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        // La API se autentica mediante Authorization: Bearer, no mediante cookies.
                        .ignoringRequestMatchers("/api/**"))
                .cors(cors -> {
                })
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        // ── Recursos estaticos y paginas publicas ──────────────────────
                        .requestMatchers(
                                "/", "/mision", "/vision", "/objetivos", "/servicios-publicos",
                                "/login", "/register", "/logout", "/error/**",
                                "/css/**", "/js/**", "/img/**", "/webjars/**", "/favicon.ico",
                                "/api/auth/**")
                        .permitAll()
                        // ── Zona ADMINISTRADOR ──────────────────────────────────────────
                        .requestMatchers("/admin/**", "/api/admin/**").hasRole("administrador")
                        // ── Zona EMPLEADO ────────────────────────────────────────────────
                        .requestMatchers("/empleado/**", "/api/empleado/**").hasRole("empleado")
                        // ── Zona CLIENTE ─────────────────────────────────────────────────
                        .requestMatchers("/cliente/**", "/api/cliente/**").hasRole("cliente")
                        // ── Servicios (consulta compartida entre admin y empleado) ─────────
                        .requestMatchers("/servicios/**").hasAnyRole("administrador", "empleado")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
