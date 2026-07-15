package br.com.samuel.documentos_academicos.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.samuel.documentos_academicos.dto.request.SolicitacaoCreateRequest;
import br.com.samuel.documentos_academicos.dto.response.SolicitacaoResponse;
import br.com.samuel.documentos_academicos.entity.Aluno;
import br.com.samuel.documentos_academicos.entity.Curso;
import br.com.samuel.documentos_academicos.entity.Solicitacao;
import br.com.samuel.documentos_academicos.entity.Status;
import br.com.samuel.documentos_academicos.entity.TipoDocumento;
import br.com.samuel.documentos_academicos.enums.Prioridade;
import br.com.samuel.documentos_academicos.exception.AlunoInativoException;
import br.com.samuel.documentos_academicos.exception.RecursoNaoEncontradoException;
import br.com.samuel.documentos_academicos.mapper.SolicitacaoMapper;
import br.com.samuel.documentos_academicos.repository.AlunoRepository;
import br.com.samuel.documentos_academicos.repository.CursoRepository;
import br.com.samuel.documentos_academicos.repository.SolicitacaoRepository;
import br.com.samuel.documentos_academicos.repository.StatusRepository;
import br.com.samuel.documentos_academicos.repository.TipoDocumentoRepository;
import br.com.samuel.documentos_academicos.service.SolicitacaoService;

@ExtendWith(MockitoExtension.class)
class SolicitacaoServiceImplTest {

    @Mock AlunoRepository alunoRepository;
    @Mock CursoRepository cursoRepository;
    @Mock TipoDocumentoRepository tipoDocumentoRepository;
    @Mock StatusRepository statusRepository;
    @Mock SolicitacaoRepository solicitacaoRepository;

    SolicitacaoService service;
    final Clock clock = Clock.fixed(Instant.parse("2026-07-14T12:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setup() {
        service = new SolicitacaoServiceImpl(alunoRepository, cursoRepository, tipoDocumentoRepository,
                statusRepository, solicitacaoRepository, new SolicitacaoMapper(), clock);
    }

    private Aluno aluno(boolean ativo) {
        Aluno a = new Aluno();
        a.setId(1L);
        a.setNome("Samuel");
        a.setAtivo(ativo);
        return a;
    }

    @Test
    void criaComStatusAbertaDatasEPrioridadePadrao() {
        Aluno a = aluno(true);
        Curso c = new Curso();
        c.setId(1L);
        c.setNome("Direito");
        TipoDocumento t = new TipoDocumento();
        t.setId(1L);
        t.setNome("Histórico");
        Status aberta = new Status();
        aberta.setId(1L);
        aberta.setCodigo("ABERTA");
        aberta.setNome("Aberta");
        aberta.setFinalizaSolicitacao(false);

        when(alunoRepository.findById(1L)).thenReturn(Optional.of(a));
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(c));
        when(tipoDocumentoRepository.findById(1L)).thenReturn(Optional.of(t));
        when(statusRepository.findByCodigoIgnoreCase("ABERTA")).thenReturn(Optional.of(aberta));
        when(solicitacaoRepository.save(any())).thenAnswer(inv -> {
            Solicitacao s = inv.getArgument(0);
            s.setId(10L);
            return s;
        });

        SolicitacaoResponse resp = service.criar(new SolicitacaoCreateRequest(1L, 1L, 1L, null));

        assertEquals("ABERTA", resp.status().codigo());
        assertEquals(Prioridade.NORMAL, resp.prioridade());
        assertNotNull(resp.dataSolicitacao());
        assertEquals(resp.dataSolicitacao(), resp.dataAlteracao());
        assertNull(resp.dataEmissao());
    }

    @Test
    void rejeitaAlunoInativo() {
        when(alunoRepository.findById(1L)).thenReturn(Optional.of(aluno(false)));
        assertThrows(AlunoInativoException.class,
                () -> service.criar(new SolicitacaoCreateRequest(1L, 1L, 1L, null)));
    }

    @Test
    void rejeitaAlunoInexistente() {
        when(alunoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class,
                () -> service.criar(new SolicitacaoCreateRequest(99L, 1L, 1L, null)));
    }
}