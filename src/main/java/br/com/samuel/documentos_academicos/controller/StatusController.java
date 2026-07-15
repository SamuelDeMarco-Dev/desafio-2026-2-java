package br.com.samuel.documentos_academicos.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.samuel.documentos_academicos.dto.request.StatusRequest;
import br.com.samuel.documentos_academicos.dto.response.StatusResponse;
import br.com.samuel.documentos_academicos.service.StatusService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/status")
public class StatusController {

    private final StatusService statusService;

    public StatusController(StatusService statusService) {
        this.statusService = statusService;
    }

    @PostMapping
    public ResponseEntity<StatusResponse> criar(@Valid @RequestBody StatusRequest request,
                                                UriComponentsBuilder uriBuilder) {
        StatusResponse response = statusService.criar(request);
        URI location = uriBuilder.path("/api/status/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public List<StatusResponse> listar() {
        return statusService.listar();
    }

    @GetMapping("/{id}")
    public StatusResponse buscar(@PathVariable Long id) {
        return statusService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public StatusResponse atualizar(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        return statusService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        statusService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
