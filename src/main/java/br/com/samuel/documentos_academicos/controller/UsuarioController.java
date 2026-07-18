package br.com.samuel.documentos_academicos.controller;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.samuel.documentos_academicos.dto.request.UsuarioAtualizacaoRequest;
import br.com.samuel.documentos_academicos.dto.request.UsuarioRequest;
import br.com.samuel.documentos_academicos.dto.request.UsuarioStatusRequest;
import br.com.samuel.documentos_academicos.dto.response.PageResponse;
import br.com.samuel.documentos_academicos.dto.response.UsuarioResponse;
import br.com.samuel.documentos_academicos.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Usuários", description = "Contas de acesso ao sistema")
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Cadastra um usuário",
               description = "Login e código de responsável são únicos. Exige perfil ADMIN.")
    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody UsuarioRequest request,
                                                 UriComponentsBuilder uriBuilder) {
        UsuarioResponse response = usuarioService.criar(request);
        URI location = uriBuilder.path("/api/usuarios/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Lista usuários com paginação",
               description = "Filtros `nome` (parcial, ignora caixa) e `ativo` são opcionais.")
    @GetMapping
    public PageResponse<UsuarioResponse> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Boolean ativo,
            @ParameterObject @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        Page<UsuarioResponse> page = usuarioService.listar(nome, ativo, pageable);
        return PageResponse.from(page);
    }

    @Operation(summary = "Consulta um usuário por id")
    @GetMapping("/{id}")
    public UsuarioResponse buscar(@PathVariable Long id) {
        return usuarioService.buscarPorId(id);
    }

    @Operation(summary = "Atualiza nome, código de responsável e perfis do usuário",
               description = "Não altera login nem senha. Exige perfil ADMIN.")
    @PutMapping("/{id}")
    public UsuarioResponse atualizar(@PathVariable Long id, @Valid @RequestBody UsuarioAtualizacaoRequest request) {
        return usuarioService.atualizar(id, request);
    }

    @Operation(summary = "Ativa ou inativa o usuário",
               description = "Usuário inativo não consegue autenticar. Exige perfil ADMIN.")
    @PatchMapping("/{id}/ativo")
    public UsuarioResponse alterarSituacao(@PathVariable Long id, @Valid @RequestBody UsuarioStatusRequest request) {
        return usuarioService.alterarSituacao(id, request.ativo());
    }
}
