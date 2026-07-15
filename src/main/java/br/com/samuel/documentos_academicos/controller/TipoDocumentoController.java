package br.com.samuel.documentos_academicos.controller;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.samuel.documentos_academicos.dto.request.TipoDocumentoRequest;
import br.com.samuel.documentos_academicos.dto.response.TipoDocumentoResponse;
import br.com.samuel.documentos_academicos.dto.response.PageResponse;
import br.com.samuel.documentos_academicos.service.TipoDocumentoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tipos-documento")
public class TipoDocumentoController {

    private final TipoDocumentoService tipoDocumentoService;

    public TipoDocumentoController(TipoDocumentoService tipoDocumentoService) {
        this.tipoDocumentoService = tipoDocumentoService;
    }

    @PostMapping
    public ResponseEntity<TipoDocumentoResponse> criar(@Valid @RequestBody TipoDocumentoRequest request,
                                                       UriComponentsBuilder uriBuilder) {
        TipoDocumentoResponse response = tipoDocumentoService.criar(request);
        URI location = uriBuilder.path("/api/tipos-documento/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public PageResponse<TipoDocumentoResponse> listar(
            @RequestParam(required = false) String nome,
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        Page<TipoDocumentoResponse> page = tipoDocumentoService.listar(nome, pageable);
        return PageResponse.from(page);
    }

    @GetMapping("/{id}")
    public TipoDocumentoResponse buscar(@PathVariable Long id) {
        return tipoDocumentoService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public TipoDocumentoResponse atualizar(@PathVariable Long id, @Valid @RequestBody TipoDocumentoRequest request) {
        return tipoDocumentoService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        tipoDocumentoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
