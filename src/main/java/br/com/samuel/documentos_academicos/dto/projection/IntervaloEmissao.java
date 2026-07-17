package br.com.samuel.documentos_academicos.dto.projection;

import java.time.LocalDateTime;

/**
 * Projeção interna de consulta — não é contrato da API.
 *
 * <p>Fica fora de {@code dto/response} de propósito: nenhum endpoint devolve este
 * registro. Ele alimenta o cálculo do tempo médio no DashboardServiceImpl, que
 * expõe o resultado como {@code TempoMedioEmissaoResponse}.
 */
public record IntervaloEmissao(LocalDateTime dataSolicitacao, LocalDateTime dataEmissao) {
}