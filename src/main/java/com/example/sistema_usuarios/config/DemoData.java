package com.example.sistema_usuarios.config;

import com.example.sistema_usuarios.model.Usuario;
import com.example.sistema_usuarios.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("demo")
public class DemoData {
  @Bean
  CommandLineRunner seed(UsuarioRepository users, PasswordEncoder encoder) {
    return args -> {
      for (String name : new String[] {"cliente", "tecnico"}) {
        if (users.existsByUsername(name)) continue;
        var user = new Usuario();
        user.setUsername(name);
        user.setDisplayName(name);
        user.setPassword(encoder.encode("Demo12345!"));
        user.setRole(name.equals("tecnico") ? "TECHNICIAN" : "USER");
        users.save(user);
      }
    };
  }
}
