package com.example.sistema_usuarios.controller;

import com.example.sistema_usuarios.model.Usuario;
import com.example.sistema_usuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://127.0.0.1:5500") // porta do Live Server
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Usuario usuario) {
        if(usuarioRepository.existsByUsername(usuario.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuário já existe!");
        }
        usuario.setPassword(encoder.encode(usuario.getPassword()));
        usuarioRepository.save(usuario);
        return ResponseEntity.ok("Usuário registrado com sucesso!");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Usuario usuario) {
        Usuario u = usuarioRepository.findByUsername(usuario.getUsername());
        if(u != null && encoder.matches(usuario.getPassword(), u.getPassword())) {
            return ResponseEntity.ok("Login realizado com sucesso!");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuário ou senha incorretos!");
        }
    }
}