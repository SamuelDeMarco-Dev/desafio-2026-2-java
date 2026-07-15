package br.com.samuel.documentos_academicos.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import br.com.samuel.documentos_academicos.dto.request.TipoDocumentoRequest;
import br.com.samuel.documentos_academicos.dto.response.TipoDocumentoResponse;

public interface TipoDocumentoService {
    TipoDocumentoResponse criar(TipoDocumentoRequest request);
    TipoDocumentoResponse buscarPorId(Long id);
    Page<TipoDocumentoResponse> listar(String nome, Pageable pageable);
    TipoDocumentoResponse atualizar(Long id, TipoDocumentoRequest request);
    void excluir(Long id);
}
