package br.com.samuel.documentos_academicos.service;

import br.com.samuel.documentos_academicos.dto.request.SolicitacaoCreateRequest;
import br.com.samuel.documentos_academicos.dto.response.SolicitacaoResponse;

public interface SolicitacaoService {
    SolicitacaoResponse criar(SolicitacaoCreateRequest request);
}
