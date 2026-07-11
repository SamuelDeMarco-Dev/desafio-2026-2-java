package br.com.samuel.documentos_academicos.dto.response;

import java.time.LocalDateTime;

import br.com.samuel.documentos_academicos.enums.Prioridade;

public record SolicitacaoResumoResponse(
        Long id,
        String alunoNome,
        String cursoNome,
        String tipoDocumentoNome,
        String statusCodigo,
        Prioridade prioridade,
        LocalDateTime dataSolicitacao) {
}