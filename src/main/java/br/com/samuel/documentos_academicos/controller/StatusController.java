package br.com.samuel.documentos_academicos.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.samuel.documentos_academicos.dto.request.StatusRequest;
import br.com.samuel.documentos_academicos.dto.response.StatusResponse;
import br.com.samuel.documentos_academicos.service.StatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Status", description = "Estados do fluxo das solicitações")
@RestController
@RequestMapping("/api/status")
public class StatusController {

    private final StatusService statusService;

    public StatusController(StatusService statusService) {
        this.statusService = statusService;
    }

    @Operation(summary = "Cadastra um status",
               description = "Código único. Exige perfil ADMIN.")
    @PostMapping
    public ResponseEntity<StatusResponse> criar(@Valid @RequestBody StatusRequest request,
                                                UriComponentsBuilder uriBuilder) {
        StatusResponse response = statusService.criar(request);
        URI location = uriBuilder.path("/api/status/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Lista todos os status",
               description = "Tabela de referência: sem paginação.")
    @GetMapping
    public List<StatusResponse> listar() {
        return statusService.listar();
    }

    @Operation(summary = "Consulta um status por id")
    @GetMapping("/{id}")
    public StatusResponse buscar(@PathVariable Long id) {
        return statusService.buscarPorId(id);
    }

    @Operation(summary = "Atualiza o status",
               description = "Os status estruturais (ABERTA, EM_ANALISE, APROVADA, EMITIDA, REPROVADA) têm código e finalização imutáveis. Exige perfil ADMIN.")
    @PutMapping("/{id}")
    public StatusResponse atualizar(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        return statusService.atualizar(id, request);
    }

    @Operation(summary = "Exclui o status",
               description = "Status estruturais e vinculados a solicitações são bloqueados com 422. Exige perfil ADMIN.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        statusService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
