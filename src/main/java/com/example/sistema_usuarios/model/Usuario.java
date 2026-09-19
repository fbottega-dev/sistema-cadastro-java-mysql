package com.example.sistema_usuarios.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 50)
  private String username;

  @Column(name = "display_name", nullable = false, length = 80)
  private String displayName;

  @JsonIgnore
  @Column(nullable = false, length = 100)
  private String password;

  @Column(nullable = false, length = 20)
  private String role = "USER";

  public Long getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String value) {
    username = value;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String value) {
    displayName = value;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String value) {
    password = value;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String value) {
    role = value;
  }
}
