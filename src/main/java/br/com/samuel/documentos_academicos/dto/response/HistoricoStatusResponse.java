package br.com.samuel.documentos_academicos.dto.response;

import java.time.LocalDateTime;

public record HistoricoStatusResponse(
        Long id,
        StatusResponse statusAnterior,   // null na abertura
        StatusResponse statusNovo,
        ResponsavelResponse responsavel,
        LocalDateTime dataMovimentacao) {
}