package br.com.samuel.documentos_academicos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

import br.com.samuel.documentos_academicos.dto.response.CursoResponse;
import br.com.samuel.documentos_academicos.exception.RecursoDuplicadoException;
import br.com.samuel.documentos_academicos.exception.RegraNegocioException;
import br.com.samuel.documentos_academicos.service.CursoService;

@WebMvcTest(CursoController.class)
public class CursoControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean CursoService cursoService;

    @Test
    void criarValidoRetorna201() throws Exception {
        when(cursoService.criar(any())).thenReturn(new CursoResponse(1L, "Direito"));
        mvc.perform(post("/api/cursos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Direito\"}"))
           .andExpect(status().isCreated())
           .andExpect(header().string("Location", "http://localhost/api/cursos/1"));
    }

    @Test
    void criarNomeVazioRetorna400() throws Exception {
        mvc.perform(post("/api/cursos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"\"}"))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.campos[0].campo").value("nome"));
    }

    @Test
    void criarDuplicadoRetorna409() throws Exception {
        when(cursoService.criar(any()))
            .thenThrow(new RecursoDuplicadoException("Já existe um curso com o nome 'Direito'"));
        mvc.perform(post("/api/cursos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Direito\"}"))
           .andExpect(status().isConflict())
           .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void excluirComVinculoRetorna422() throws Exception {
        org.mockito.Mockito.doThrow(new RegraNegocioException("Curso vinculado a solicitações"))
            .when(cursoService).excluir(1L);
        mvc.perform(delete("/api/cursos/1")).andExpect(status().isUnprocessableEntity());
    }
}
