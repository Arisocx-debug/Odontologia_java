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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.http.HttpMethod;
import org.springframework.web.filter.HiddenHttpMethodFilter;

import com.wilsonmontenegro.odontologia.security.GoogleOAuth2User;
import com.wilsonmontenegro.odontologia.security.GoogleOAuth2UserService;
import com.wilsonmontenegro.odontologia.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService usuarioDetailsService;
    private final GoogleOAuth2UserService googleOAuth2UserService;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final AccessDeniedHandler accessDeniedHandler;
    private final ClientRegistrationRepository clientRegistrationRepository;

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(usuarioDetailsService);
        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // Filtro necesario para DELETE/PUT/PATCH en formularios HTML
    @Bean
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter();
    }

    @Bean
    public OAuth2AuthorizationRequestResolver authorizationRequestResolver(
        ClientRegistrationRepository clientRegistrationRepository) {

     DefaultOAuth2AuthorizationRequestResolver resolver =
            new DefaultOAuth2AuthorizationRequestResolver(
                    clientRegistrationRepository,
                    "/oauth2/authorization"
            );

     resolver.setAuthorizationRequestCustomizer(
            customizer -> customizer.additionalParameters(
                    params -> params.put("prompt", "select_account")
            )
    );

    return resolver;
    }

    @Bean
    public SecurityFilterChain filterChain(
        HttpSecurity http,
        OAuth2AuthorizationRequestResolver authorizationRequestResolver
    ) throws Exception {

        http
                // CSRF para formularios Thymeleaf
                .csrf(csrf -> csrf
                        .csrfTokenRepository(
                                CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers("/api/**")
                )

                // Sesiones SOLO para vistas Thymeleaf
                .sessionManagement(sm -> sm
                        .sessionCreationPolicy(
                                SessionCreationPolicy.IF_REQUIRED))

                // El logout lo hace AuthWebController
                .logout(logout -> logout.disable())

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))

                .authorizeHttpRequests(auth -> auth

                        // Público
                        .requestMatchers(
                                "/",
                                "/mision",
                                "/vision",
                                "/objetivos",
                                "/servicios-publicos",
                                "/servicios/publicos",
                                "/login",
                                "/register",
                                "/logout",
                                "/error/**",
                                "/css/**",
                                "/js/**",
                                "/img/**",
                                "/uploads/**",
                                "/webjars/**",
                                "/favicon.ico",
                                "/api/auth/**",
                                "/oauth2/**",
                                "/login/oauth2/**"
                        )
                        .permitAll()

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
                        .requestMatchers("/api/**")
                        .authenticated()

                        .anyRequest()
                        .authenticated()
                )

                .oauth2Login(oauth2 -> oauth2

                        .authorizationEndpoint(endpoint ->
                                endpoint.authorizationRequestResolver(
                                        authorizationRequestResolver
                                )
                        )
                
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(googleOAuth2UserService)
                        )
                
                        .successHandler((request, response, authentication) -> {
                
                            GoogleOAuth2User principal =
                                    (GoogleOAuth2User) authentication.getPrincipal();
                
                            switch (principal.getUsuario().getRol()) {
                
                                case ADMINISTRADOR:
                                    response.sendRedirect("/admin/dashboard");
                                    break;
                
                                case EMPLEADO:
                                    response.sendRedirect("/empleado/dashboard");
                                    break;
                
                                case CLIENTE:
                                    response.sendRedirect("/cliente/citas");
                                    break;
                
                                default:
                                    response.sendRedirect("/");
                                    break;
                            }
                        })
                )

                // Filtro JWT SOLO para API
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}