package br.com.samuel.documentos_academicos.service;

import java.time.LocalDate;
import java.util.List;

import br.com.samuel.documentos_academicos.dto.response.ContagemStatusResponse;
import br.com.samuel.documentos_academicos.dto.response.ContagemTipoDocumentoResponse;
import br.com.samuel.documentos_academicos.dto.response.DashboardResumoResponse;
import br.com.samuel.documentos_academicos.dto.response.TempoMedioEmissaoResponse;

public interface DashboardService {
    DashboardResumoResponse resumo(LocalDate inicio, LocalDate fim);
    List<ContagemStatusResponse> solicitacoesPorStatus(LocalDate inicio, LocalDate fim);
    List<ContagemTipoDocumentoResponse> documentosMaisSolicitados(LocalDate inicio, LocalDate fim);
    TempoMedioEmissaoResponse tempoMedioEmissao(LocalDate inicio, LocalDate fim);
}