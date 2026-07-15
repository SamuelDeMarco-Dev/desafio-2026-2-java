package br.com.samuel.documentos_academicos.dto.response;

import java.util.List;

public record DashboardResumoResponse(
        long total,
        List<ContagemStatusResponse> porStatus,
        TempoMedioEmissaoResponse tempoMedioEmissao) {
}