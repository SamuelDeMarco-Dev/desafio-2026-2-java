package br.com.samuel.documentos_academicos.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import br.com.samuel.documentos_academicos.dto.request.SolicitacaoFiltro;
import br.com.samuel.documentos_academicos.entity.Aluno;
import br.com.samuel.documentos_academicos.entity.Curso;
import br.com.samuel.documentos_academicos.entity.Solicitacao;
import br.com.samuel.documentos_academicos.entity.Status;
import br.com.samuel.documentos_academicos.entity.TipoDocumento;
import br.com.samuel.documentos_academicos.enums.Prioridade;
import br.com.samuel.documentos_academicos.repository.SolicitacaoRepository;
import br.com.samuel.documentos_academicos.service.RelatorioService;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceImplTest {

    @Mock SolicitacaoRepository solicitacaoRepository;

    RelatorioService service;
    final Clock clock = Clock.fixed(Instant.parse("2026-07-14T12:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setup() {
        service = new RelatorioServiceImpl(solicitacaoRepository, clock);
    }

    @SuppressWarnings("unchecked")
    private void repositorioDevolve(List<Solicitacao> solicitacoes) {
        when(solicitacaoRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn((Page<Solicitacao>) new PageImpl<>(solicitacoes));
    }

    private Solicitacao solicitacao(long id, String alunoNome, Prioridade prioridade) {
        Aluno aluno = new Aluno();
        aluno.setId(id);
        aluno.setNome(alunoNome);
        aluno.setAtivo(true);
        Curso curso = new Curso();
        curso.setId(1L);
        curso.setNome("Direito");
        TipoDocumento tipo = new TipoDocumento();
        tipo.setId(1L);
        tipo.setNome("Histórico Escolar");
        Status status = new Status();
        status.setId(1L);
        status.setCodigo("ABERTA");
        status.setNome("Aberta");
        status.setFinalizaSolicitacao(false);

        Solicitacao s = new Solicitacao();
        s.setId(id);
        s.setAluno(aluno);
        s.setCurso(curso);
        s.setTipoDocumento(tipo);
        s.setStatus(status);
        s.setPrioridade(prioridade);
        s.setDataSolicitacao(LocalDateTime.now(clock));
        s.setDataAlteracao(LocalDateTime.now(clock));
        return s;
    }

    /** Compila o JRXML real do classpath e exporta um PDF de verdade. */
    @Test
    void geraPdfValidoComDados() {
        repositorioDevolve(List.of(
                solicitacao(1L, "Samuel", Prioridade.URGENTE),
                solicitacao(2L, "Maria", Prioridade.NORMAL)));

        byte[] pdf = service.gerarSolicitacoesPdf(new SolicitacaoFiltro(null, null, null, null, null, null, null));

        String cabecalho = new String(pdf, 0, 5, StandardCharsets.US_ASCII);
        assertEquals("%PDF-", cabecalho, "o arquivo deve começar com a assinatura PDF");
        assertTrue(pdf.length > 1000, "PDF com conteúdo deve ter tamanho relevante");
    }

    @Test
    void geraPdfMesmoSemResultados() {
        repositorioDevolve(List.of());

        byte[] pdf = service.gerarSolicitacoesPdf(
                new SolicitacaoFiltro("ninguem", null, null, null, null, null, null));

        assertEquals("%PDF-", new String(pdf, 0, 5, StandardCharsets.US_ASCII));
    }
}