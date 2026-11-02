package com.hostelhub.config;

import com.hostelhub.security.JwtAuthenticationEntryPoint;
import com.hostelhub.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public & Preflight Endpoints
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/rooms", "/api/rooms/**", "/api/rules", "/api/rules/**", "/api/hostel/qr").permitAll()
                        
                        // Gate & QR Endpoints
                        .requestMatchers("/api/gate/scan", "/api/gate/my-logs").hasAnyRole("STUDENT", "ADMIN", "WARDEN")
                        .requestMatchers("/api/gate/warden/logs").hasAnyRole("WARDEN", "ADMIN")
                        .requestMatchers("/api/gate/admin/logs").hasRole("ADMIN")
                        
                        // Admin Specific Endpoints
                        .requestMatchers("/api/dashboard/admin").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/wardens", "/api/wardens/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/wardens", "/api/wardens/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/wardens", "/api/wardens/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/wardens", "/api/wardens/**").hasAnyRole("ADMIN", "WARDEN", "STUDENT")
                        
                        // Warden & Admin Endpoints
                        .requestMatchers("/api/dashboard/warden", "/api/dashboard/warden/**").hasAnyRole("ADMIN", "WARDEN")
                        .requestMatchers(HttpMethod.PUT, "/api/leaves/*/approve", "/api/leaves/*/reject", "/api/leaves/*/action", "/api/leaves/**").hasAnyRole("ADMIN", "WARDEN")
                        .requestMatchers(HttpMethod.POST, "/api/rooms/allocate").hasAnyRole("ADMIN", "WARDEN")
                        .requestMatchers(HttpMethod.POST, "/api/rooms").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/rooms/**").hasRole("ADMIN")
                        
                        // Student & Authenticated Endpoints
                        .requestMatchers("/api/dashboard/student/**").hasAnyRole("STUDENT", "ADMIN", "WARDEN")
                        .requestMatchers(HttpMethod.GET, "/api/students", "/api/students/**").hasAnyRole("STUDENT", "ADMIN", "WARDEN")
                        .requestMatchers(HttpMethod.GET, "/api/complaints", "/api/complaints/**").hasAnyRole("STUDENT", "ADMIN", "WARDEN")
                        .requestMatchers(HttpMethod.GET, "/api/leaves", "/api/leaves/**").hasAnyRole("STUDENT", "ADMIN", "WARDEN")
                        .requestMatchers(HttpMethod.POST, "/api/leaves").hasAnyRole("STUDENT", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/complaints").hasAnyRole("STUDENT", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/complaints/**").hasAnyRole("STUDENT", "ADMIN", "WARDEN")
                        .requestMatchers(HttpMethod.PUT, "/api/students/**").hasAnyRole("STUDENT", "ADMIN", "WARDEN")
                        
                        // Any other request
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
