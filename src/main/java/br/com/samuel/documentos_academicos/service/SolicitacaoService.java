package br.com.samuel.documentos_academicos.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import br.com.samuel.documentos_academicos.dto.request.SolicitacaoCreateRequest;
import br.com.samuel.documentos_academicos.dto.request.SolicitacaoFiltro;
import br.com.samuel.documentos_academicos.dto.response.SolicitacaoResponse;
import br.com.samuel.documentos_academicos.dto.response.SolicitacaoResumoResponse;

public interface SolicitacaoService {
    SolicitacaoResponse criar(SolicitacaoCreateRequest request);
    SolicitacaoResponse buscarPorId(Long id);
    Page<SolicitacaoResumoResponse> listar(SolicitacaoFiltro filtro, Pageable pageable);
}
