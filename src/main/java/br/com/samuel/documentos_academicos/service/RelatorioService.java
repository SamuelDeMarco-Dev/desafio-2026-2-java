package br.com.samuel.documentos_academicos.service;

import br.com.samuel.documentos_academicos.dto.request.SolicitacaoFiltro;

public interface RelatorioService {
    byte[] gerarSolicitacoesPdf(SolicitacaoFiltro filtro);
}