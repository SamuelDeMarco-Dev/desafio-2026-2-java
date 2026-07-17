package br.com.samuel.documentos_academicos.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.samuel.documentos_academicos.dto.response.ContagemStatusResponse;
import br.com.samuel.documentos_academicos.dto.response.ContagemTipoDocumentoResponse;
import br.com.samuel.documentos_academicos.dto.response.DashboardResumoResponse;
import br.com.samuel.documentos_academicos.dto.response.TempoMedioEmissaoResponse;
import br.com.samuel.documentos_academicos.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Dashboard", description = "Indicadores agregados das solicitações")
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Operation(summary = "Resumo geral do período",
               description = "Total de solicitações, contagem por status e tempo médio de emissão. O período (`dataInicio`/`dataFim`, ISO yyyy-MM-dd) é opcional; sem ele considera toda a base.")
    @GetMapping("/resumo")
    public DashboardResumoResponse resumo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        return dashboardService.resumo(dataInicio, dataFim);
    }

    @Operation(summary = "Quantidade de solicitações por status",
               description = "Ordenado da maior contagem para a menor. O período (`dataInicio`/`dataFim`, ISO yyyy-MM-dd) é opcional; sem ele considera toda a base.")
    @GetMapping("/solicitacoes-por-status")
    public List<ContagemStatusResponse> porStatus(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        return dashboardService.solicitacoesPorStatus(dataInicio, dataFim);
    }

    @Operation(summary = "Ranking dos tipos de documento mais solicitados",
               description = "O período (`dataInicio`/`dataFim`, ISO yyyy-MM-dd) é opcional; sem ele considera toda a base.")
    @GetMapping("/documentos-mais-solicitados")
    public List<ContagemTipoDocumentoResponse> documentos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        return dashboardService.documentosMaisSolicitados(dataInicio, dataFim);
    }

    @Operation(summary = "Tempo médio entre solicitação e emissão",
               description = "Em dias (fracionário). Considera apenas solicitações emitidas. O período (`dataInicio`/`dataFim`, ISO yyyy-MM-dd) é opcional; sem ele considera toda a base.")
    @GetMapping("/tempo-medio-emissao")
    public TempoMedioEmissaoResponse tempoMedio(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        return dashboardService.tempoMedioEmissao(dataInicio, dataFim);
    }
}