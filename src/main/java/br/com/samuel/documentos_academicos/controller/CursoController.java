package br.com.samuel.documentos_academicos.controller;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.samuel.documentos_academicos.dto.request.CursoRequest;
import br.com.samuel.documentos_academicos.dto.response.CursoResponse;
import br.com.samuel.documentos_academicos.dto.response.PageResponse;
import br.com.samuel.documentos_academicos.service.CursoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cursos")
public class CursoController {
    
    private final CursoService cursoService;

    public CursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    @PostMapping
    public ResponseEntity<CursoResponse> criar(@Valid @RequestBody CursoRequest request,
                                                UriComponentsBuilder uriBuilder) {
        CursoResponse response = cursoService.criar(request);
        URI location = uriBuilder.path("/api/cursos/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }   

    @GetMapping
    public PageResponse<CursoResponse> listar(
        @RequestParam(required = false) String nome,
        @PageableDefault(size = 20, sort = "nome") Pageable pageable){
    Page<CursoResponse> page = cursoService.listar(nome, pageable);
    return PageResponse.from(page);
    }   

        @GetMapping("/{id}")
    public CursoResponse buscar(@PathVariable Long id) {
        return cursoService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public CursoResponse atualizar(@PathVariable Long id, @Valid @RequestBody CursoRequest request) {
        return cursoService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        cursoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
