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

import br.com.samuel.documentos_academicos.dto.response.StatusResponse;
import br.com.samuel.documentos_academicos.exception.RegraNegocioException;
import br.com.samuel.documentos_academicos.service.StatusService;

@WebMvcTest(StatusController.class)
class StatusControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean StatusService statusService;

    @Test
    void criarValidoRetorna201() throws Exception {
        when(statusService.criar(any()))
            .thenReturn(new StatusResponse(6L, "URGENTE_REVISAO", "Urgente para revisão", 1001, false));
        mvc.perform(post("/api/status").contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigo\":\"URGENTE_REVISAO\",\"nome\":\"Urgente para revisão\",\"responsavel\":1001,\"finalizaSolicitacao\":false}"))
           .andExpect(status().isCreated())
           .andExpect(header().string("Location", "http://localhost/api/status/6"));
    }

    @Test
    void criarCamposObrigatoriosVaziosRetorna400() throws Exception {
        mvc.perform(post("/api/status").contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigo\":\"\",\"nome\":\"\"}"))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.campos").isNotEmpty());
    }

    @Test
    void excluirEstruturalRetorna422() throws Exception {
        org.mockito.Mockito.doThrow(new RegraNegocioException("Status estrutural do fluxo não pode ser removido."))
            .when(statusService).excluir(1L);
        mvc.perform(delete("/api/status/1")).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void listarRetorna200() throws Exception {
        when(statusService.listar()).thenReturn(java.util.List.of(
            new StatusResponse(1L, "ABERTA", "Aberta", null, false)));
        mvc.perform(get("/api/status"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0].codigo").value("ABERTA"));
    }
}

