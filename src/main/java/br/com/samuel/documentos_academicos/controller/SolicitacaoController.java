package br.com.samuel.documentos_academicos.controller;

import java.net.URI;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.samuel.documentos_academicos.dto.request.AlteracaoStatusRequest;
import br.com.samuel.documentos_academicos.dto.request.SolicitacaoCreateRequest;
import br.com.samuel.documentos_academicos.dto.request.SolicitacaoFiltro;
import br.com.samuel.documentos_academicos.dto.response.HistoricoStatusResponse;
import br.com.samuel.documentos_academicos.dto.response.PageResponse;
import br.com.samuel.documentos_academicos.dto.response.SolicitacaoResponse;
import br.com.samuel.documentos_academicos.dto.response.SolicitacaoResumoResponse;
import br.com.samuel.documentos_academicos.service.SolicitacaoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/solicitacoes")
public class SolicitacaoController {

    private final SolicitacaoService solicitacaoService;

    public SolicitacaoController(SolicitacaoService solicitacaoService) {
        this.solicitacaoService = solicitacaoService;
    }

    @PostMapping
    public ResponseEntity<SolicitacaoResponse> criar(@Valid @RequestBody SolicitacaoCreateRequest request,
                                                     UriComponentsBuilder uriBuilder) {
        SolicitacaoResponse response = solicitacaoService.criar(request);
        URI location = uriBuilder.path("/api/solicitacoes/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    public SolicitacaoResponse buscar(@PathVariable Long id) {
        return solicitacaoService.buscarPorId(id);
    }

    @GetMapping
    public PageResponse<SolicitacaoResumoResponse> listar(
            SolicitacaoFiltro filtro,
            @PageableDefault(size = 20, sort = "dataSolicitacao", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<SolicitacaoResumoResponse> page = solicitacaoService.listar(filtro, pageable);
        return PageResponse.from(page);
    }

    @PatchMapping("/{id}/status")
    public SolicitacaoResponse alterarStatus(@PathVariable Long id,
                                         @Valid @RequestBody AlteracaoStatusRequest request) {
        return solicitacaoService.alterarStatus(id, request);
    }

    @GetMapping("/{id}/historico")
    public List<HistoricoStatusResponse> historico(@PathVariable Long id) {
        return solicitacaoService.historico(id);
    }

}