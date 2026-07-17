package br.com.samuel.documentos_academicos.controller;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.samuel.documentos_academicos.dto.request.TipoDocumentoRequest;
import br.com.samuel.documentos_academicos.dto.response.TipoDocumentoResponse;
import br.com.samuel.documentos_academicos.dto.response.PageResponse;
import br.com.samuel.documentos_academicos.service.TipoDocumentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Tipos de documento", description = "Cadastro dos tipos de documento emitíveis")
@RestController
@RequestMapping("/api/tipos-documento")
public class TipoDocumentoController {

    private final TipoDocumentoService tipoDocumentoService;

    public TipoDocumentoController(TipoDocumentoService tipoDocumentoService) {
        this.tipoDocumentoService = tipoDocumentoService;
    }

    @Operation(summary = "Cadastra um tipo de documento",
               description = "Nome único; duplicado devolve 409. Exige perfil ADMIN.")
    @PostMapping
    public ResponseEntity<TipoDocumentoResponse> criar(@Valid @RequestBody TipoDocumentoRequest request,
                                                       UriComponentsBuilder uriBuilder) {
        TipoDocumentoResponse response = tipoDocumentoService.criar(request);
        URI location = uriBuilder.path("/api/tipos-documento/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Lista tipos de documento com paginação",
               description = "Filtro `nome` (parcial, ignora caixa) é opcional.")
    @GetMapping
    public PageResponse<TipoDocumentoResponse> listar(
            @RequestParam(required = false) String nome,
            @ParameterObject @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        Page<TipoDocumentoResponse> page = tipoDocumentoService.listar(nome, pageable);
        return PageResponse.from(page);
    }

    @Operation(summary = "Consulta um tipo de documento por id")
    @GetMapping("/{id}")
    public TipoDocumentoResponse buscar(@PathVariable Long id) {
        return tipoDocumentoService.buscarPorId(id);
    }

    @Operation(summary = "Atualiza o tipo de documento",
               description = "Mantém a unicidade do nome. Exige perfil ADMIN.")
    @PutMapping("/{id}")
    public TipoDocumentoResponse atualizar(@PathVariable Long id, @Valid @RequestBody TipoDocumentoRequest request) {
        return tipoDocumentoService.atualizar(id, request);
    }

    @Operation(summary = "Exclui o tipo de documento",
               description = "Bloqueado com 422 se vinculado a solicitações. Exige perfil ADMIN.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        tipoDocumentoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
