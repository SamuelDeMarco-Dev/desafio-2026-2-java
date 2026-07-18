package br.com.samuel.documentos_academicos.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.samuel.documentos_academicos.dto.request.SolicitacaoFiltro;
import br.com.samuel.documentos_academicos.service.RelatorioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Relatórios", description = "Relatórios em PDF")
@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @Operation(summary = "Relatório PDF das solicitações",
               description = "Aceita os mesmos filtros da listagem (todos opcionais). "
                           + "Sem filtro de `status`, solicitações encerradas ficam fora; "
                           + "a ordenação é por prioridade e depois pela mais recente.")
    @GetMapping(value = "/solicitacoes", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> solicitacoes(@ParameterObject SolicitacaoFiltro filtro) {
        byte[] pdf = relatorioService.gerarSolicitacoesPdf(filtro);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("relatorio-solicitacoes.pdf")
                        .build().toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}