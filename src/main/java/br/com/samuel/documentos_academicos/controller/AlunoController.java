package br.com.samuel.documentos_academicos.controller;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.samuel.documentos_academicos.dto.request.AlunoRequest;
import br.com.samuel.documentos_academicos.dto.request.AlunoStatusRequest;
import br.com.samuel.documentos_academicos.dto.response.AlunoResponse;
import br.com.samuel.documentos_academicos.dto.response.PageResponse;
import br.com.samuel.documentos_academicos.dto.response.SolicitacaoResumoResponse;
import br.com.samuel.documentos_academicos.service.AlunoService;
import br.com.samuel.documentos_academicos.service.SolicitacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Alunos", description = "Cadastro de alunos e consulta de suas solicitações")
@RestController
@RequestMapping("/api/alunos")
public class AlunoController {

    private final AlunoService alunoService;
    private final SolicitacaoService solicitacaoService;

    public AlunoController(AlunoService alunoService, SolicitacaoService solicitacaoService) {
        this.alunoService = alunoService;
        this.solicitacaoService = solicitacaoService;
    }

    @Operation(summary = "Cadastra um aluno",
               description = "Exige perfil ADMIN.")
    @PostMapping
    public ResponseEntity<AlunoResponse> criar(@Valid @RequestBody AlunoRequest request,
                                               UriComponentsBuilder uriBuilder){
        AlunoResponse response = alunoService.criar(request);
        URI location = uriBuilder.path("/api/alunos/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Lista alunos com paginação",
               description = "Filtros `nome` (parcial, ignora caixa) e `ativo` são opcionais.")
    @GetMapping
    public PageResponse<AlunoResponse> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Boolean ativo,
            @ParameterObject @PageableDefault(size = 20, sort = "nome") Pageable pageable){
        Page<AlunoResponse> page = alunoService.listar(nome, ativo, pageable);
        return PageResponse.from(page);
    }

    @Operation(summary = "Consulta um aluno por id")
    @GetMapping("/{id}")
    public AlunoResponse buscar(@PathVariable Long id){
        return alunoService.buscarPorId(id);
    }

    @Operation(summary = "Lista as solicitações de um aluno",
               description = "Paginado. Aluno inexistente devolve 404.")
    @GetMapping("/{id}/solicitacoes")
    public PageResponse<SolicitacaoResumoResponse> solicitacoesDoAluno(
            @PathVariable Long id,
            @ParameterObject @PageableDefault(size = 20, sort = "dataSolicitacao", direction = Sort.Direction.DESC) Pageable pageable){
        return PageResponse.from(solicitacaoService.listarPorAluno(id, pageable));
    }

    @Operation(summary = "Atualiza o nome do aluno",
               description = "Exige perfil ADMIN.")
    @PutMapping("/{id}")
    public AlunoResponse atualizar(@PathVariable Long id, @Valid @RequestBody AlunoRequest request){
        return alunoService.atualizar(id, request);
    }

    @Operation(summary = "Ativa ou inativa o aluno",
               description = "Aluno inativo não pode abrir solicitações. Exige perfil ADMIN.")
    @PatchMapping("/{id}/ativo")
    public AlunoResponse alterarSituacao(@PathVariable Long id,
                                         @Valid @RequestBody AlunoStatusRequest request) {
        return alunoService.alterarSituacao(id, request.ativo());
    }

    @Operation(summary = "Exclui o aluno",
               description = "Bloqueado com 422 se houver solicitações vinculadas. Exige perfil ADMIN.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id){
        alunoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}