package com.example.sistema_usuarios.controller;

import com.example.sistema_usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
public class UsuarioController {
  private final UsuarioService users;

  public UsuarioController(UsuarioService users) {
    this.users = users;
  }

  public record Registration(
      @NotBlank @Pattern(regexp = "[a-zA-Z0-9._-]{3,50}") String username,
      @NotBlank @Size(min = 8, max = 64) @Pattern(regexp = "[\\x20-\\x7E]+") String password) {}

  @GetMapping("/csrf")
  public CsrfToken csrf(CsrfToken token) {
    return token;
  }

  @PostMapping("/usuarios/register")
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, Object> register(@Valid @RequestBody Registration input) {
    var user = users.register(input.username(), input.password());
    return Map.of("id", user.getId(), "username", user.getUsername());
  }

  @GetMapping({"/usuarios/me", "/usuarios/home"})
  public Map<String, Object> me(Authentication auth) {
    return Map.of(
        "username",
        auth.getName(),
        "roles",
        auth.getAuthorities().stream().map(Object::toString).toList());
  }
}
