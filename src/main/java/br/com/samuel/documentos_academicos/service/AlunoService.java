package br.com.samuel.documentos_academicos.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import br.com.samuel.documentos_academicos.dto.request.AlunoRequest;
import br.com.samuel.documentos_academicos.dto.response.AlunoResponse;

public interface AlunoService {
    AlunoResponse criar(AlunoRequest request);
    AlunoResponse buscarPorId(Long id);
    Page<AlunoResponse> listar(String nome, Boolean ativo, Pageable pageable);
    AlunoResponse atualizar(Long id, AlunoRequest resquest);
    AlunoResponse alterarSituacao(Long id, boolean ativo);
    void excluir(Long id);
}
