package br.com.samuel.documentos_academicos.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import br.com.samuel.documentos_academicos.dto.request.CursoRequest;
import br.com.samuel.documentos_academicos.dto.response.CursoResponse;

public interface CursoService {
    CursoResponse criar(CursoRequest request);
    CursoResponse buscarPorId(Long Id);
    Page<CursoResponse> listar(String nome, Pageable pageable);
    CursoResponse atualizar(Long id, CursoRequest request);
    void excluir(Long id);
}