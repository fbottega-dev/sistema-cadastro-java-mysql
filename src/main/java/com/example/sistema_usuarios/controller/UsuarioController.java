package com.example.sistema_usuarios.controller;

import com.example.sistema_usuarios.model.Usuario;
import com.example.sistema_usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
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

  public record ProfileUpdate(@NotBlank @Size(min = 2, max = 80) String displayName) {
    public ProfileUpdate {
      if (displayName != null) displayName = displayName.strip();
    }
  }

  public record Profile(String username, String displayName, List<String> roles) {}

  @GetMapping("/csrf")
  public CsrfToken csrf(CsrfToken token) {
    return token;
  }

  @PostMapping("/usuarios/register")
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, Object> register(@Valid @RequestBody Registration input) {
    var user = users.register(input.username(), input.password());
    return Map.of(
        "id", user.getId(), "username", user.getUsername(), "displayName", user.getDisplayName());
  }

  @GetMapping({"/usuarios/me", "/usuarios/home"})
  public Profile me(Authentication auth) {
    return profile(auth, users.findByUsername(auth.getName()));
  }

  @PatchMapping("/usuarios/me")
  public Profile updateProfile(Authentication auth, @Valid @RequestBody ProfileUpdate input) {
    return profile(auth, users.updateDisplayName(auth.getName(), input.displayName()));
  }

  private Profile profile(Authentication auth, Usuario user) {
    return new Profile(
        user.getUsername(),
        user.getDisplayName(),
        auth.getAuthorities().stream().map(Object::toString).toList());
  }
}
