package br.com.samuel.documentos_academicos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.samuel.documentos_academicos.dto.response.TipoDocumentoResponse;
import br.com.samuel.documentos_academicos.exception.RecursoDuplicadoException;
import br.com.samuel.documentos_academicos.exception.RegraNegocioException;
import br.com.samuel.documentos_academicos.service.TipoDocumentoService;

@WebMvcTest(TipoDocumentoController.class)
@AutoConfigureMockMvc(addFilters = false)
class TipoDocumentoControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean TipoDocumentoService tipoDocumentoService;

    @Test
    void criarValidoRetorna201() throws Exception {
        when(tipoDocumentoService.criar(any())).thenReturn(new TipoDocumentoResponse(1L, "Histórico Escolar"));
        mvc.perform(post("/api/tipos-documento").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Histórico Escolar\"}"))
           .andExpect(status().isCreated())
           .andExpect(header().string("Location", "http://localhost/api/tipos-documento/1"));
    }

    @Test
    void criarNomeVazioRetorna400() throws Exception {
        mvc.perform(post("/api/tipos-documento").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"\"}"))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.campos[0].campo").value("nome"));
    }

    @Test
    void criarDuplicadoRetorna409() throws Exception {
        when(tipoDocumentoService.criar(any()))
            .thenThrow(new RecursoDuplicadoException("Já existe um tipo de documento com o nome 'Histórico Escolar'"));
        mvc.perform(post("/api/tipos-documento").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Histórico Escolar\"}"))
           .andExpect(status().isConflict())
           .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void excluirComVinculoRetorna422() throws Exception {
        doThrow(new RegraNegocioException("Tipo vinculado a solicitações"))
            .when(tipoDocumentoService).excluir(1L);
        mvc.perform(delete("/api/tipos-documento/1")).andExpect(status().isUnprocessableEntity());
    }
}