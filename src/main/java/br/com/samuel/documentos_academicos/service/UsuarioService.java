package br.com.samuel.documentos_academicos.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import br.com.samuel.documentos_academicos.dto.request.UsuarioAtualizacaoRequest;
import br.com.samuel.documentos_academicos.dto.request.UsuarioRequest;
import br.com.samuel.documentos_academicos.dto.response.UsuarioResponse;

public interface UsuarioService {
    UsuarioResponse criar(UsuarioRequest request);
    UsuarioResponse buscarPorId(Long id);
    Page<UsuarioResponse> listar(String nome, Boolean ativo, Pageable pageable);
    UsuarioResponse atualizar(Long id, UsuarioAtualizacaoRequest request);
    UsuarioResponse alterarSituacao(Long id, boolean ativo);
}
