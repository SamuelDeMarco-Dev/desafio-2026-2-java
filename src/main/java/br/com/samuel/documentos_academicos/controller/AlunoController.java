package br.com.samuel.documentos_academicos.controller;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.samuel.documentos_academicos.dto.request.AlunoRequest;
import br.com.samuel.documentos_academicos.dto.request.AlunoStatusRequest;
import br.com.samuel.documentos_academicos.dto.response.AlunoResponse;
import br.com.samuel.documentos_academicos.dto.response.PageResponse;
import br.com.samuel.documentos_academicos.service.AlunoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/alunos")
public class AlunoController {
    
    private final AlunoService alunoService;

    public AlunoController(AlunoService alunoService){
        this.alunoService = alunoService;
    }

    @PostMapping
    public ResponseEntity<AlunoResponse> criar(@Valid @RequestBody AlunoRequest request, 
                                               UriComponentsBuilder uriBuilder){
        AlunoResponse response = alunoService.criar(request);
        URI location = uriBuilder.path("/api/alunos/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public PageResponse<AlunoResponse> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Boolean ativo,
            @PageableDefault(size = 20, sort = "nome") Pageable pageable){
        Page<AlunoResponse> page = alunoService.listar(nome, ativo, pageable);
        return PageResponse.from(page);
    }

    @GetMapping("/{id}")
    public AlunoResponse buscar(@PathVariable Long id){
        return alunoService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public AlunoResponse atualizar(@PathVariable Long id, @Valid @RequestBody AlunoRequest request){
        return alunoService.atualizar(id, request);
    }

    @PatchMapping("/{id}/ativo")
    public AlunoResponse alterarSituacao(@PathVariable Long id,
                                         @Valid @RequestBody AlunoStatusRequest request) {
        return alunoService.alterarSituacao(id, request.ativo());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id){
        alunoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
