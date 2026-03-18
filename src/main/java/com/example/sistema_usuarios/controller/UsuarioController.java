package com.example.sistema_usuarios.controller;

import com.example.sistema_usuarios.model.Usuario;
import com.example.sistema_usuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private BCryptPasswordEncoder encoder;

    // Criar usuário
    @PostMapping
    public Usuario criar(@RequestBody Usuario usuario) {
        usuario.setSenha(encoder.encode(usuario.getSenha()));
        return repository.save(usuario);
    }

    // Listar usuários (liberado para teste)
    @GetMapping
    public List<Usuario> listar() {
        return repository.findAll();
    }

    // LOGIN SIMPLES 
    @PostMapping("/login")
    public String login(@RequestBody Usuario usuario) {
        Optional<Usuario> user = repository.findByEmail(usuario.getEmail());

        if (user.isPresent() && encoder.matches(usuario.getSenha(), user.get().getSenha())) {
            return "Login OK!";  //  Retorna apenas uma mensagem de sucesso
        }

        return "Email ou senha inválidos"; //  Login falhou
    }
}