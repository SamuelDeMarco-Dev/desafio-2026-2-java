package br.com.samuel.documentos_academicos.service.impl;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.samuel.documentos_academicos.dto.response.ContagemStatusResponse;
import br.com.samuel.documentos_academicos.dto.response.ContagemTipoDocumentoResponse;
import br.com.samuel.documentos_academicos.dto.response.DashboardResumoResponse;
import br.com.samuel.documentos_academicos.dto.response.EmissaoIntervalo;
import br.com.samuel.documentos_academicos.dto.response.TempoMedioEmissaoResponse;
import br.com.samuel.documentos_academicos.repository.SolicitacaoRepository;
import br.com.samuel.documentos_academicos.service.DashboardService;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final SolicitacaoRepository solicitacaoRepository;

    public DashboardServiceImpl(SolicitacaoRepository solicitacaoRepository) {
        this.solicitacaoRepository = solicitacaoRepository;
    }

    @Override
    public DashboardResumoResponse resumo(LocalDate inicio, LocalDate fim) {
        return new DashboardResumoResponse(
                solicitacaoRepository.contarNoPeriodo(inicioDe(inicio), fimDe(fim)),
                solicitacoesPorStatus(inicio, fim),
                tempoMedioEmissao(inicio, fim));
    }

    @Override
    public List<ContagemStatusResponse> solicitacoesPorStatus(LocalDate inicio, LocalDate fim) {
        return solicitacaoRepository.contarPorStatus(inicioDe(inicio), fimDe(fim));
    }

    @Override
    public List<ContagemTipoDocumentoResponse> documentosMaisSolicitados(LocalDate inicio, LocalDate fim) {
        return solicitacaoRepository.documentosMaisSolicitados(inicioDe(inicio), fimDe(fim));
    }

    @Override
    public TempoMedioEmissaoResponse tempoMedioEmissao(LocalDate inicio, LocalDate fim) {
        List<EmissaoIntervalo> intervalos = solicitacaoRepository.intervalosEmissao(inicioDe(inicio), fimDe(fim));
        double diasMedios = intervalos.stream()
                .mapToLong(i -> Duration.between(i.dataSolicitacao(), i.dataEmissao()).toSeconds())
                .average()
                .orElse(0) / 86400.0; // segundos -> dias (fracionário)
        return new TempoMedioEmissaoResponse(diasMedios, intervalos.size());
    }

    private LocalDateTime inicioDe(LocalDate data) {
        return data != null ? data.atStartOfDay() : null;
    }

    private LocalDateTime fimDe(LocalDate data) {
        return data != null ? data.plusDays(1).atStartOfDay() : null; // fim inclusivo
    }
}