package com.example.sistema_usuarios.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // desativa CSRF para teste simples
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/usuarios/register").permitAll() // libera registro
                .requestMatchers("/usuarios/login").permitAll() // libera login
                .anyRequest().authenticated()
            )
            .formLogin().disable(); // desativa o login form automático do Spring

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }
}