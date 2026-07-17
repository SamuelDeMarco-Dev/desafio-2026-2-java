package br.com.samuel.documentos_academicos.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.samuel.documentos_academicos.dto.projection.IntervaloEmissao;
import br.com.samuel.documentos_academicos.dto.response.TempoMedioEmissaoResponse;
import br.com.samuel.documentos_academicos.repository.SolicitacaoRepository;
import br.com.samuel.documentos_academicos.service.DashboardService;

/**
 * Indicadores no nível unitário: sem banco, só a lógica do service.
 * O DashboardQueriesTest cobre o SQL; aqui cobrimos o cálculo e a
 * tradução do período.
 */
@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock SolicitacaoRepository solicitacaoRepository;

    DashboardService service;

    @BeforeEach
    void setup() {
        service = new DashboardServiceImpl(solicitacaoRepository);
    }

    @Test
    void tempoMedioConverteSegundosParaDiasFracionarios() {
        LocalDateTime pedido = LocalDateTime.of(2026, 7, 1, 0, 0);
        when(solicitacaoRepository.intervalosEmissao(any(), any())).thenReturn(List.of(
                new IntervaloEmissao(pedido, pedido.plusDays(2)),   // 2 dias
                new IntervaloEmissao(pedido, pedido.plusDays(3))));  // 3 dias

        TempoMedioEmissaoResponse resp = service.tempoMedioEmissao(null, null);

        assertEquals(2.5, resp.diasMedios(), 0.0001);
        assertEquals(2, resp.totalEmitidas());
    }

    @Test
    void tempoMedioComMeioDiaNaoArredondaParaInteiro() {
        LocalDateTime pedido = LocalDateTime.of(2026, 7, 1, 0, 0);
        when(solicitacaoRepository.intervalosEmissao(any(), any()))
                .thenReturn(List.of(new IntervaloEmissao(pedido, pedido.plusHours(12))));

        assertEquals(0.5, service.tempoMedioEmissao(null, null).diasMedios(), 0.0001);
    }

    @Test
    void semEmitidasTempoMedioEZeroENaoDivisaoPorZero() {
        when(solicitacaoRepository.intervalosEmissao(any(), any())).thenReturn(List.of());

        TempoMedioEmissaoResponse resp = service.tempoMedioEmissao(null, null);

        assertEquals(0.0, resp.diasMedios(), 0.0001);
        assertEquals(0, resp.totalEmitidas());
    }

    /**
     * Trava a correção do bug de nulos sem tipo: o service traduz "sem período"
     * para limites amplos, porque o PostgreSQL não infere o tipo de um
     * parâmetro nulo e a consulta quebra.
     */
    @Test
    void periodoNaoInformadoViraIntervaloAmploNuncaNulo() {
        when(solicitacaoRepository.intervalosEmissao(any(), any())).thenReturn(List.of());

        service.tempoMedioEmissao(null, null);

        ArgumentCaptor<LocalDateTime> inicio = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> fim = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(solicitacaoRepository).intervalosEmissao(inicio.capture(), fim.capture());

        assertEquals(LocalDateTime.of(1900, 1, 1, 0, 0), inicio.getValue());
        assertEquals(LocalDateTime.of(9999, 12, 31, 0, 0), fim.getValue());
    }

    @Test
    void dataFimEInclusiva() {
        when(solicitacaoRepository.intervalosEmissao(any(), any())).thenReturn(List.of());

        service.tempoMedioEmissao(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));

        ArgumentCaptor<LocalDateTime> inicio = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> fim = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(solicitacaoRepository).intervalosEmissao(inicio.capture(), fim.capture());

        assertEquals(LocalDateTime.of(2026, 7, 1, 0, 0), inicio.getValue());
        // 1º de agosto às 00:00 como limite exclusivo = 31 de julho inteiro incluído
        assertEquals(LocalDateTime.of(2026, 8, 1, 0, 0), fim.getValue());
    }

    @Test
    void resumoAgregaContagemStatusETempoMedio() {
        when(solicitacaoRepository.contarNoPeriodo(any(), any())).thenReturn(7L);
        when(solicitacaoRepository.contarPorStatus(any(), any())).thenReturn(List.of());
        when(solicitacaoRepository.intervalosEmissao(any(), any())).thenReturn(List.of());

        var resumo = service.resumo(null, null);

        assertEquals(7L, resumo.total());
    }
}