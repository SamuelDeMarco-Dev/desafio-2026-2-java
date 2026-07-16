package br.com.samuel.documentos_academicos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.samuel.documentos_academicos.dto.response.TokenResponse;
import br.com.samuel.documentos_academicos.exception.CredenciaisInvalidasException;
import br.com.samuel.documentos_academicos.service.AuthService;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired MockMvc mvc;

    @MockitoBean AuthService authService;

    @Test
    void loginValidoRetorna200ComToken() throws Exception {
        when(authService.autenticar(any()))
                .thenReturn(new TokenResponse("Bearer", "token-jwt-fake", 3600));

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"administrador\",\"senha\":\"admin12345\"}"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.tipo").value("Bearer"))
           .andExpect(jsonPath("$.token").value("token-jwt-fake"))
           .andExpect(jsonPath("$.expiraEm").value(3600));
    }

    @Test
    void credenciaisInvalidasRetorna401() throws Exception {
        when(authService.autenticar(any()))
                .thenThrow(new CredenciaisInvalidasException("Login ou senha inválidos"));

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"administrador\",\"senha\":\"errada\"}"))
           .andExpect(status().isUnauthorized())
           .andExpect(jsonPath("$.status").value(401))
           .andExpect(jsonPath("$.erro").value("Credenciais inválidas"));
    }

    @Test
    void loginSemSenhaRetorna400() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"administrador\"}"))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.campos[0].campo").value("senha"));
    }
}