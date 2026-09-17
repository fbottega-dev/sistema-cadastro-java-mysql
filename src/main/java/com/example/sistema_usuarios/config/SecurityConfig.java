package com.example.sistema_usuarios.config;

import com.example.sistema_usuarios.repository.UsuarioRepository;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
  @Bean
  PasswordEncoder encoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  UserDetailsService userDetails(UsuarioRepository users) {
    return username ->
        users
            .findByUsername(username)
            .map(
                u ->
                    User.withUsername(u.getUsername())
                        .password(u.getPassword())
                        .roles(u.getRole())
                        .build())
            .orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas"));
  }

  @Bean
  SecurityFilterChain security(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/",
                        "/index.html",
                        "/app.js",
                        "/style.css",
                        "/csrf",
                        "/usuarios/register",
                        "/usuarios/login",
                        "/error")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .formLogin(
            form ->
                form.loginProcessingUrl("/usuarios/login")
                    .successHandler(
                        (req, res, auth) -> {
                          res.setContentType("application/json");
                          res.getWriter().write("{\"message\":\"Autenticado\"}");
                        })
                    .failureHandler((req, res, error) -> res.sendError(401)))
        .logout(
            logout ->
                logout
                    .logoutUrl("/usuarios/logout")
                    .logoutSuccessHandler((req, res, auth) -> res.setStatus(204)))
        .exceptionHandling(
            errors -> errors.authenticationEntryPoint((req, res, error) -> res.sendError(401)));
    return http.build();
  }
}
