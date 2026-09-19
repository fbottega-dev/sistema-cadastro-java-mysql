package com.example.sistema_usuarios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.sistema_usuarios.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProfileTests {
  private static final String PASSWORD = "Password123!";

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired UsuarioRepository users;

  @Test
  void registrationStartsWithLoginAsDisplayName() throws Exception {
    var username = register();
    var session = login(username);
    for (var path : new String[] {"/usuarios/me", "/usuarios/home"}) {
      mvc.perform(get(path).session(session))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.username").value(username))
          .andExpect(jsonPath("$.displayName").value(username))
          .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
          .andExpect(jsonPath("$.password").doesNotExist());
    }
  }

  @Test
  void editPersistsAfterNewLoginAndKeepsCredentials() throws Exception {
    var username = register();
    var before = users.findByUsername(username).orElseThrow();
    var session = login(username);

    update(session, "  João da Silva  ")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value(username))
        .andExpect(jsonPath("$.displayName").value("João da Silva"))
        .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
        .andExpect(jsonPath("$.password").doesNotExist());

    var after = users.findByUsername(username).orElseThrow();
    assertThat(after.getId()).isEqualTo(before.getId());
    assertThat(after.getUsername()).isEqualTo(before.getUsername());
    assertThat(after.getPassword()).isEqualTo(before.getPassword());
    assertThat(after.getRole()).isEqualTo(before.getRole());
    assertThat(after.getDisplayName()).isEqualTo("João da Silva");

    mvc.perform(post("/usuarios/logout").session(session).with(csrf()))
        .andExpect(status().isNoContent());
    var newSession = login(username);
    for (var path : new String[] {"/usuarios/me", "/usuarios/home"}) {
      mvc.perform(get(path).session(newSession))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.username").value(username))
          .andExpect(jsonPath("$.displayName").value("João da Silva"));
    }
  }

  @Test
  void invalidNamesDoNotChangeSavedName() throws Exception {
    var username = register();
    var session = login(username);
    for (var name : new String[] {null, "", "   ", " A ", "a".repeat(81)}) {
      update(session, name).andExpect(status().isBadRequest());
      assertThat(users.findByUsername(username).orElseThrow().getDisplayName()).isEqualTo(username);
    }
  }

  @Test
  void sizeLimitsApplyAfterRemovingOuterWhitespace() throws Exception {
    var username = register();
    var session = login(username);
    for (var name : new String[] {"Lu", "Á".repeat(80)}) {
      update(session, " \t" + name + " \n")
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.displayName").value(name));
      assertThat(users.findByUsername(username).orElseThrow().getDisplayName()).isEqualTo(name);
    }
  }

  @Test
  void extraFieldsCannotChangeCredentialsOrAnotherAccount() throws Exception {
    var username = register();
    var otherUsername = register();
    var before = users.findByUsername(username).orElseThrow();
    var other = users.findByUsername(otherUsername).orElseThrow();
    var session = login(username);
    var payload =
        Map.of(
            "displayName",
            "Nome atualizado",
            "id",
            other.getId(),
            "username",
            otherUsername,
            "password",
            "Changed123!",
            "role",
            "ADMIN",
            "roles",
            new String[] {"ROLE_ADMIN"});
    mvc.perform(
            patch("/usuarios/me")
                .session(session)
                .with(csrf())
                .contentType("application/json")
                .content(json.writeValueAsString(payload)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value(username))
        .andExpect(jsonPath("$.displayName").value("Nome atualizado"))
        .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));

    var after = users.findById(before.getId()).orElseThrow();
    assertThat(after.getUsername()).isEqualTo(username);
    assertThat(after.getPassword()).isEqualTo(before.getPassword());
    assertThat(after.getRole()).isEqualTo("USER");
    assertThat(after.getDisplayName()).isEqualTo("Nome atualizado");
    var otherAfter = users.findById(other.getId()).orElseThrow();
    assertThat(otherAfter.getDisplayName()).isEqualTo(otherUsername);
    assertThat(otherAfter.getUsername()).isEqualTo(other.getUsername());
    assertThat(otherAfter.getPassword()).isEqualTo(other.getPassword());
    assertThat(otherAfter.getRole()).isEqualTo(other.getRole());
  }

  @Test
  void editRequiresAuthenticatedSessionAndCsrf() throws Exception {
    mvc.perform(
            patch("/usuarios/me")
                .with(csrf())
                .contentType("application/json")
                .content("{\"displayName\":\"Novo nome\"}"))
        .andExpect(status().isUnauthorized());

    var username = register();
    var session = login(username);
    mvc.perform(
            patch("/usuarios/me")
                .session(session)
                .contentType("application/json")
                .content("{\"displayName\":\"Novo nome\"}"))
        .andExpect(status().isForbidden());
    mvc.perform(
            patch("/usuarios/me")
                .session(session)
                .with(csrf().useInvalidToken())
                .contentType("application/json")
                .content("{\"displayName\":\"Novo nome\"}"))
        .andExpect(status().isForbidden());
    assertThat(users.findByUsername(username).orElseThrow().getDisplayName()).isEqualTo(username);
  }

  private String register() throws Exception {
    var username = "profile" + UUID.randomUUID().toString().replace("-", "");
    mvc.perform(
            post("/usuarios/register")
                .with(csrf())
                .contentType("application/json")
                .content(
                    json.writeValueAsString(Map.of("username", username, "password", PASSWORD))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.username").value(username))
        .andExpect(jsonPath("$.displayName").value(username))
        .andExpect(jsonPath("$.password").doesNotExist());
    return username;
  }

  private MockHttpSession login(String username) throws Exception {
    var result =
        mvc.perform(
                post("/usuarios/login")
                    .with(csrf())
                    .param("username", username)
                    .param("password", PASSWORD))
            .andExpect(status().isOk())
            .andReturn();
    return (MockHttpSession) result.getRequest().getSession(false);
  }

  private ResultActions update(MockHttpSession session, String name) throws Exception {
    return mvc.perform(
        patch("/usuarios/me")
            .session(session)
            .with(csrf())
            .contentType("application/json")
            .content(json.writeValueAsString(Collections.singletonMap("displayName", name))));
  }
}
