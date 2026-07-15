package br.com.samuel.documentos_academicos.dto.request;

import java.time.LocalDate;

import br.com.samuel.documentos_academicos.enums.Prioridade;

public record SolicitacaoFiltro(
        String aluno,
        String curso,
        String tipoDocumento,
        String status,
        Prioridade prioridade,
        LocalDate dataInicio,
        LocalDate dataFim) {
}