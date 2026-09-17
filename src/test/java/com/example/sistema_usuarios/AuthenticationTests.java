package com.example.sistema_usuarios;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationTests {
  @Autowired MockMvc mvc;

  @Test
  void anonymousCannotReadProfile() throws Exception {
    mvc.perform(get("/usuarios/me")).andExpect(status().isUnauthorized());
  }

  @Test
  void registrationRequiresCsrf() throws Exception {
    mvc.perform(post("/usuarios/register").contentType("application/json").content("{}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void validatesInput() throws Exception {
    mvc.perform(
            post("/usuarios/register")
                .with(csrf())
                .contentType("application/json")
                .content("{\"username\":\"a\",\"password\":\"123\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void registerLoginSessionAndLogout() throws Exception {
    var body = "{\"username\":\"alice\",\"password\":\"Password123!\"}";
    mvc.perform(
            post("/usuarios/register").with(csrf()).contentType("application/json").content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.password").doesNotExist());
    mvc.perform(
            post("/usuarios/register").with(csrf()).contentType("application/json").content(body))
        .andExpect(status().isConflict());
    mvc.perform(
            post("/usuarios/login")
                .with(csrf())
                .param("username", "alice")
                .param("password", "wrong"))
        .andExpect(status().isUnauthorized());
    var result =
        mvc.perform(
                post("/usuarios/login")
                    .with(csrf())
                    .param("username", "alice")
                    .param("password", "Password123!"))
            .andExpect(status().isOk())
            .andReturn();
    var session = (MockHttpSession) result.getRequest().getSession(false);
    mvc.perform(get("/usuarios/me").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("alice"));
    mvc.perform(post("/usuarios/logout").session(session).with(csrf()))
        .andExpect(status().isNoContent());
    mvc.perform(get("/usuarios/me")).andExpect(status().isUnauthorized());
  }
}
