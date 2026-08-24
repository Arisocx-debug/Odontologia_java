package com.wilsonmontenegro.odontologia.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import com.wilsonmontenegro.odontologia.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.web.filter.HiddenHttpMethodFilter;

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

    // 🔥 Filtro necesario para DELETE/PUT/PATCH en formularios HTML
    @Bean
public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
    return new HiddenHttpMethodFilter();
}

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // CSRF para formularios Thymeleaf
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers("/api/**") // API sin CSRF
            )

            // Sesiones SOLO para vistas Thymeleaf
            .sessionManagement(sm -> sm
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )

            .authenticationProvider(authenticationProvider())

            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )

            .authorizeHttpRequests(auth -> auth

                // Público
                .requestMatchers(
                    "/", "/mision", "/vision", "/objetivos", "/servicios-publicos","/servicios/publicos",
                    "/login", "/register", "/logout", "/error/**",
                    "/css/**", "/js/**", "/img/**", "/webjars/**", "/favicon.ico",
                    "/api/auth/**"
                ).permitAll()

                // INVENTARIO
                .requestMatchers("/inventario/**")
                    .hasAnyRole("ADMINISTRADOR", "EMPLEADO")

                .requestMatchers(HttpMethod.DELETE, "/inventario/**")
                    .hasAnyRole("ADMINISTRADOR", "EMPLEADO")

                .requestMatchers(HttpMethod.PUT, "/inventario/**")
                    .hasAnyRole("ADMINISTRADOR", "EMPLEADO")

                .requestMatchers(HttpMethod.PATCH, "/inventario/**")
                    .hasAnyRole("ADMINISTRADOR", "EMPLEADO")

                // ADMIN
                .requestMatchers("/admin/**", "/api/admin/**")
                    .hasRole("ADMINISTRADOR")

                // EMPLEADO
                .requestMatchers("/empleado/**", "/api/empleado/**")
                    .hasRole("EMPLEADO")

                // CLIENTE
                .requestMatchers("/cliente/**", "/api/cliente/**")
                    .hasRole("CLIENTE")

                // Servicios compartidos
                .requestMatchers("/servicios/**")
                    .hasAnyRole("ADMINISTRADOR", "EMPLEADO")

                // API con JWT
                .requestMatchers("/api/**").authenticated()

                .anyRequest().authenticated()
            )

            // Filtro JWT SOLO para API
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}


