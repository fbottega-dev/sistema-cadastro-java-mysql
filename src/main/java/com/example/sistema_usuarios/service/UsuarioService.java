package com.example.sistema_usuarios.service;

import com.example.sistema_usuarios.model.Usuario;
import com.example.sistema_usuarios.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService {
  private final UsuarioRepository users;
  private final PasswordEncoder encoder;

  public UsuarioService(UsuarioRepository users, PasswordEncoder encoder) {
    this.users = users;
    this.encoder = encoder;
  }

  @Transactional
  public Usuario register(String username, String password) {
    if (users.existsByUsername(username))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Usuário já cadastrado");
    var user = new Usuario();
    user.setUsername(username);
    user.setPassword(encoder.encode(password));
    return users.saveAndFlush(user);
  }
}
