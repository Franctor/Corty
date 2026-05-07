package com.corty.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        // ── Preflight ──────────────────────────────────────────
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ── Público ────────────────────────────────────────────
                        .requestMatchers("/api/payments/webhook").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/auth/activate").permitAll()
                        .requestMatchers("/api/location/**").permitAll()
                        .requestMatchers("/api/media/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/sports/filters").permitAll()

                        // ── Solo ADMIN / SUPERADMIN ────────────────────────────
                        // Gestión de deportes y superficies (escritura solo ADMIN)
                        .requestMatchers(HttpMethod.POST,   "/api/sports/**").hasAnyRole("ADMIN", "SUPERADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/sports/**").hasAnyRole("ADMIN", "SUPERADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/sports/**").hasAnyRole("ADMIN", "SUPERADMIN")
                        .requestMatchers(HttpMethod.POST,   "/api/surfaces/**").hasAnyRole("ADMIN", "SUPERADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/surfaces/**").hasAnyRole("ADMIN", "SUPERADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/surfaces/**").hasAnyRole("ADMIN", "SUPERADMIN")
                        // Gestión de usuarios
                        .requestMatchers("/api/users/**").hasAnyRole("ADMIN", "SUPERADMIN")
                        // Gestión de players (admin)
                        .requestMatchers("/api/players/admin/**").hasAnyRole("ADMIN", "SUPERADMIN")
                        // Gestión de organizaciones (solo admin puede crear/editar/borrar)
                        .requestMatchers(HttpMethod.POST,   "/api/organizations/**").hasAnyRole("ADMIN", "SUPERADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/organizations/**").hasAnyRole("ADMIN", "SUPERADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/organizations/**").hasAnyRole("ADMIN", "SUPERADMIN")

                        // ── ADMIN, SUPERADMIN u ORGANIZATION ──────────────────
                        // Gestión de clubes
                        .requestMatchers(HttpMethod.POST,   "/api/clubs/**").hasAnyRole("ADMIN", "SUPERADMIN", "ORGANIZATION")
                        .requestMatchers(HttpMethod.PUT,    "/api/clubs/**").hasAnyRole("ADMIN", "SUPERADMIN", "ORGANIZATION")
                        .requestMatchers(HttpMethod.DELETE, "/api/clubs/**").hasAnyRole("ADMIN", "SUPERADMIN", "ORGANIZATION")
                        // Gestión de pistas
                        .requestMatchers(HttpMethod.POST,   "/api/courts/**").hasAnyRole("ADMIN", "SUPERADMIN", "ORGANIZATION")
                        .requestMatchers(HttpMethod.PUT,    "/api/courts/**").hasAnyRole("ADMIN", "SUPERADMIN", "ORGANIZATION")
                        .requestMatchers(HttpMethod.DELETE, "/api/courts/**").hasAnyRole("ADMIN", "SUPERADMIN", "ORGANIZATION")

                        // ── Cualquier usuario autenticado ──────────────────────
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of(
                "http://localhost:4200",
                "http://localhost:4201",
                "http://localhost:8100",
                "https://corty-gilt.vercel.app",
                "https://admin-web-gold-eight.vercel.app",
                "capacitor://localhost",
                "https://localhost",
                "http://localhost"));

        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowCredentials(true);
        config.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
