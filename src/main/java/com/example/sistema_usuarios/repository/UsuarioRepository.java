package com.example.sistema_usuarios.repository;

import com.example.sistema_usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    boolean existsByUsername(String username);
    Usuario findByUsername(String username);
}