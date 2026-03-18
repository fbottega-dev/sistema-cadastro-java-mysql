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
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // libera apenas rota de cadastro, se quiser
                .requestMatchers("/usuarios/register").permitAll() 
                // qualquer outra rota exige login
                .anyRequest().authenticated()
            )
            // ativa o formLogin automático do Spring Security
            .formLogin()  
            .defaultSuccessUrl("/usuarios/home", true) // opcional: página após login
            .permitAll()
            // logout permitido para todos
            .and()
            .logout().permitAll();

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }
}