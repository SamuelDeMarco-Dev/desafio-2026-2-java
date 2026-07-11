package br.com.samuel.documentos_academicos.dto.response;

import java.time.LocalDateTime;

import br.com.samuel.documentos_academicos.enums.Prioridade;

public record SolicitacaoResponse(
        Long id,
        AlunoResponse aluno,
        CursoResponse curso,
        TipoDocumentoResponse tipoDocumento,
        StatusResponse status,
        Prioridade prioridade,
        LocalDateTime dataSolicitacao,
        LocalDateTime dataAlteracao,
        LocalDateTime dataEmissao) {
}