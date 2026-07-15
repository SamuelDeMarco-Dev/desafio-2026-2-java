package br.com.samuel.documentos_academicos.dto.response;

import java.time.LocalDateTime;

public record EmissaoIntervalo(LocalDateTime dataSolicitacao, LocalDateTime dataEmissao) {
}