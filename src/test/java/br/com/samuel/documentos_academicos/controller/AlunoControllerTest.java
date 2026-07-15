package br.com.samuel.documentos_academicos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.samuel.documentos_academicos.dto.response.AlunoResponse;
import br.com.samuel.documentos_academicos.exception.RecursoNaoEncontradoException;
import br.com.samuel.documentos_academicos.service.AlunoService;
import br.com.samuel.documentos_academicos.service.SolicitacaoService;

@WebMvcTest(AlunoController.class)
class AlunoControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    AlunoService alunoService;

    @MockitoBean
    SolicitacaoService solicitacaoService;

    @Test
    void criarComNomeValidoRetorna201() throws Exception {
        when(alunoService.criar(any())).thenReturn(new AlunoResponse(1L, "Samuel", true));
        mvc.perform(post("/api/alunos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Samuel\"}"))
           .andExpect(status().isCreated())
           .andExpect(header().string("Location", "http://localhost/api/alunos/1"))
           .andExpect(jsonPath("$.ativo").value(true));
    }

    @Test
    void criarComNomeVazioRetorna400() throws Exception {
        mvc.perform(post("/api/alunos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"\"}"))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.campos[0].campo").value("nome"));
    }

    @Test
    void buscarInexistenteRetorna404() throws Exception {
        when(alunoService.buscarPorId(99L))
            .thenThrow(new RecursoNaoEncontradoException("Aluno 99 não encontrado"));
        mvc.perform(get("/api/alunos/99")).andExpect(status().isNotFound());
    }

    @Test
    void excluirRetorna204() throws Exception {
        mvc.perform(delete("/api/alunos/1")).andExpect(status().isNoContent());
    }
}