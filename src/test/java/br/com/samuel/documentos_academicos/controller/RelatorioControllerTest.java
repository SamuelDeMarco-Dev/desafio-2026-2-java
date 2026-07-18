package br.com.samuel.documentos_academicos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import br.com.samuel.documentos_academicos.dto.request.SolicitacaoFiltro;
import br.com.samuel.documentos_academicos.service.RelatorioService;

@WebMvcTest(RelatorioController.class)
@AutoConfigureMockMvc(addFilters = false)
class RelatorioControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean RelatorioService relatorioService;

    @Test
    void devolvePdfComHeadersDeDownload() throws Exception {
        when(relatorioService.gerarSolicitacoesPdf(any())).thenReturn("%PDF-fake".getBytes());

        mvc.perform(get("/api/relatorios/solicitacoes"))
           .andExpect(status().isOk())
           .andExpect(content().contentType(MediaType.APPLICATION_PDF))
           .andExpect(header().string("Content-Disposition",
                   "attachment; filename=\"relatorio-solicitacoes.pdf\""));
    }

    @Test
    void repassaOsFiltrosDaQueryString() throws Exception {
        when(relatorioService.gerarSolicitacoesPdf(any())).thenReturn("%PDF-fake".getBytes());

        mvc.perform(get("/api/relatorios/solicitacoes?status=EMITIDA&aluno=Samuel"))
           .andExpect(status().isOk());

        ArgumentCaptor<SolicitacaoFiltro> filtro = ArgumentCaptor.forClass(SolicitacaoFiltro.class);
        org.mockito.Mockito.verify(relatorioService).gerarSolicitacoesPdf(filtro.capture());
        assertEquals("EMITIDA", filtro.getValue().status());
        assertEquals("Samuel", filtro.getValue().aluno());
    }
}