package br.com.samuel.documentos_academicos.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.samuel.documentos_academicos.dto.request.AlteracaoStatusRequest;
import br.com.samuel.documentos_academicos.dto.request.SolicitacaoCreateRequest;
import br.com.samuel.documentos_academicos.dto.response.SolicitacaoResponse;
import br.com.samuel.documentos_academicos.entity.Aluno;
import br.com.samuel.documentos_academicos.entity.Curso;
import br.com.samuel.documentos_academicos.entity.HistoricoStatus;
import br.com.samuel.documentos_academicos.entity.Solicitacao;
import br.com.samuel.documentos_academicos.entity.Status;
import br.com.samuel.documentos_academicos.entity.TipoDocumento;
import br.com.samuel.documentos_academicos.entity.Usuario;
import br.com.samuel.documentos_academicos.enums.Prioridade;
import br.com.samuel.documentos_academicos.exception.AlunoInativoException;
import br.com.samuel.documentos_academicos.exception.RecursoNaoEncontradoException;
import br.com.samuel.documentos_academicos.exception.RegraNegocioException;
import br.com.samuel.documentos_academicos.exception.ResponsavelInvalidoException;
import br.com.samuel.documentos_academicos.exception.TransicaoStatusInvalidaException;
import br.com.samuel.documentos_academicos.mapper.HistoricoStatusMapper;
import br.com.samuel.documentos_academicos.mapper.SolicitacaoMapper;
import br.com.samuel.documentos_academicos.repository.AlunoRepository;
import br.com.samuel.documentos_academicos.repository.CursoRepository;
import br.com.samuel.documentos_academicos.repository.HistoricoStatusRepository;
import br.com.samuel.documentos_academicos.repository.SolicitacaoRepository;
import br.com.samuel.documentos_academicos.repository.StatusRepository;
import br.com.samuel.documentos_academicos.repository.TipoDocumentoRepository;
import br.com.samuel.documentos_academicos.security.UsuarioAutenticadoProvider;
import br.com.samuel.documentos_academicos.service.SolicitacaoService;

@ExtendWith(MockitoExtension.class)
class SolicitacaoServiceImplTest {

    private static final Integer CODIGO_AUTENTICADO = 1000;

    @Mock AlunoRepository alunoRepository;
    @Mock CursoRepository cursoRepository;
    @Mock TipoDocumentoRepository tipoDocumentoRepository;
    @Mock StatusRepository statusRepository;
    @Mock SolicitacaoRepository solicitacaoRepository;
    @Mock HistoricoStatusRepository historicoStatusRepository;
    @Mock UsuarioAutenticadoProvider usuarioAutenticadoProvider;

    SolicitacaoService service;
    final Clock clock = Clock.fixed(Instant.parse("2026-07-14T12:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setup() {
        service = new SolicitacaoServiceImpl(alunoRepository, cursoRepository, tipoDocumentoRepository,
                statusRepository, solicitacaoRepository, historicoStatusRepository,
                new SolicitacaoMapper(), new HistoricoStatusMapper(),
                usuarioAutenticadoProvider, clock);
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
        Status aberta = status(1L, "ABERTA", false);

        when(alunoRepository.findById(1L)).thenReturn(Optional.of(a));
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(c));
        when(tipoDocumentoRepository.findById(1L)).thenReturn(Optional.of(t));
        when(statusRepository.findByCodigoIgnoreCase("ABERTA")).thenReturn(Optional.of(aberta));
        when(usuarioAutenticadoProvider.obter()).thenReturn(autenticado(CODIGO_AUTENTICADO));
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

    // ----- fluxo de status (Issue 19) -----

    private Usuario autenticado(Integer codigo) {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setNome("Administrador");
        u.setLogin("administrador");
        u.setCodigoResponsavel(codigo);
        u.setAtivo(true);
        return u;
    }

    private Status status(Long id, String codigo, boolean finaliza) {
        Status s = new Status();
        s.setId(id);
        s.setCodigo(codigo);
        s.setNome(codigo);
        s.setFinalizaSolicitacao(finaliza);
        return s;
    }

    private Solicitacao solicitacaoEm(Status atual) {
        Solicitacao s = new Solicitacao();
        s.setId(10L);
        s.setAluno(aluno(true));
        Curso c = new Curso();
        c.setId(1L);
        c.setNome("Direito");
        s.setCurso(c);
        TipoDocumento t = new TipoDocumento();
        t.setId(1L);
        t.setNome("Histórico");
        s.setTipoDocumento(t);
        s.setStatus(atual);
        s.setPrioridade(Prioridade.NORMAL);
        LocalDateTime antes = LocalDateTime.of(2026, 1, 1, 8, 0);
        s.setDataSolicitacao(antes);
        s.setDataAlteracao(antes);
        return s;
    }

    /** Prepara o cenário comum: solicitação em `atual`, destino `destino`, usuário autenticado válido. */
    private void cenario(Status atual, Status destino) {
        when(solicitacaoRepository.findById(10L)).thenReturn(Optional.of(solicitacaoEm(atual)));
        when(statusRepository.findById(destino.getId())).thenReturn(Optional.of(destino));
        when(usuarioAutenticadoProvider.obter()).thenReturn(autenticado(CODIGO_AUTENTICADO));
    }

    private void devolveOQueSalvou() {
        when(solicitacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private AlteracaoStatusRequest pedido(Status destino) {
        return new AlteracaoStatusRequest(destino.getId(), CODIGO_AUTENTICADO);
    }

    @Test
    void aberta_paraEmAnalise_atualizaDataAlteracaoESemEmissao() {
        Status destino = status(2L, "EM_ANALISE", false);
        cenario(status(1L, "ABERTA", false), destino);
        devolveOQueSalvou();

        SolicitacaoResponse resp = service.alterarStatus(10L, pedido(destino));

        assertEquals("EM_ANALISE", resp.status().codigo());
        assertEquals(LocalDateTime.now(clock), resp.dataAlteracao());
        assertNull(resp.dataEmissao());
    }

    @Test
    void aprovada_paraEmitida_preencheDataEmissao() {
        Status destino = status(4L, "EMITIDA", true);
        cenario(status(3L, "APROVADA", false), destino);
        devolveOQueSalvou();

        SolicitacaoResponse resp = service.alterarStatus(10L, pedido(destino));

        assertEquals("EMITIDA", resp.status().codigo());
        assertEquals(LocalDateTime.now(clock), resp.dataEmissao());
    }

    @Test
    void emAnalise_paraReprovada_mantemDataEmissaoNula() {
        Status destino = status(5L, "REPROVADA", true);
        cenario(status(2L, "EM_ANALISE", false), destino);
        devolveOQueSalvou();

        SolicitacaoResponse resp = service.alterarStatus(10L, pedido(destino));

        assertEquals("REPROVADA", resp.status().codigo());
        assertNull(resp.dataEmissao());
    }

    @Test
    void aberta_paraEmitida_lancaTransicaoInvalida() {
        Status destino = status(4L, "EMITIDA", true);
        cenario(status(1L, "ABERTA", false), destino);

        assertThrows(TransicaoStatusInvalidaException.class, () -> service.alterarStatus(10L, pedido(destino)));
    }

    @Test
    void solicitacaoEmitida_naoPodeSerMovimentada() {
        Status destino = status(1L, "ABERTA", false);
        cenario(status(4L, "EMITIDA", true), destino);

        assertThrows(RegraNegocioException.class, () -> service.alterarStatus(10L, pedido(destino)));
    }

    @Test
    void codigoResponsavelDiferenteDoAutenticado_lancaResponsavelInvalido() {
        Status destino = status(2L, "EM_ANALISE", false);
        cenario(status(1L, "ABERTA", false), destino);

        AlteracaoStatusRequest request = new AlteracaoStatusRequest(destino.getId(), 9999);
        assertThrows(ResponsavelInvalidoException.class, () -> service.alterarStatus(10L, request));
    }

    @Test
    void statusComResponsavelDefinido_bloqueiaOutroUsuario() {
        Status destino = status(2L, "EM_ANALISE", false);
        destino.setResponsavel(2000); // pertence a outro responsável
        cenario(status(1L, "ABERTA", false), destino);

        assertThrows(ResponsavelInvalidoException.class, () -> service.alterarStatus(10L, pedido(destino)));
    }

    // ----- histórico (Issue 20) -----

    @Test
    void criar_registraAberturaComStatusAnteriorNulo() {
        Aluno a = aluno(true);
        Curso c = new Curso();
        c.setId(1L);
        c.setNome("Direito");
        TipoDocumento t = new TipoDocumento();
        t.setId(1L);
        t.setNome("Histórico");
        Status aberta = status(1L, "ABERTA", false);

        when(alunoRepository.findById(1L)).thenReturn(Optional.of(a));
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(c));
        when(tipoDocumentoRepository.findById(1L)).thenReturn(Optional.of(t));
        when(statusRepository.findByCodigoIgnoreCase("ABERTA")).thenReturn(Optional.of(aberta));
        when(usuarioAutenticadoProvider.obter()).thenReturn(autenticado(CODIGO_AUTENTICADO));
        when(solicitacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.criar(new SolicitacaoCreateRequest(1L, 1L, 1L, null));

        ArgumentCaptor<HistoricoStatus> captor = ArgumentCaptor.forClass(HistoricoStatus.class);
        verify(historicoStatusRepository).save(captor.capture());
        HistoricoStatus h = captor.getValue();
        assertNull(h.getStatusAnterior());
        assertEquals("ABERTA", h.getStatusNovo().getCodigo());
        assertEquals(CODIGO_AUTENTICADO, h.getUsuario().getCodigoResponsavel());
        assertEquals(LocalDateTime.now(clock), h.getDataMovimentacao());
    }

    @Test
    void alterarStatus_registraAnteriorNovoUsuarioEData() {
        Status destino = status(2L, "EM_ANALISE", false);
        cenario(status(1L, "ABERTA", false), destino);
        devolveOQueSalvou();

        service.alterarStatus(10L, pedido(destino));

        ArgumentCaptor<HistoricoStatus> captor = ArgumentCaptor.forClass(HistoricoStatus.class);
        verify(historicoStatusRepository).save(captor.capture());
        HistoricoStatus h = captor.getValue();
        assertEquals("ABERTA", h.getStatusAnterior().getCodigo());
        assertEquals("EM_ANALISE", h.getStatusNovo().getCodigo());
        assertEquals(CODIGO_AUTENTICADO, h.getUsuario().getCodigoResponsavel());
        assertEquals(LocalDateTime.now(clock), h.getDataMovimentacao());
        assertEquals(10L, h.getSolicitacao().getId());
    }

    @Test
    void alterarStatus_recusado_naoRegistraHistorico() {
        Status destino = status(4L, "EMITIDA", true);
        cenario(status(1L, "ABERTA", false), destino);

        assertThrows(TransicaoStatusInvalidaException.class, () -> service.alterarStatus(10L, pedido(destino)));
        verifyNoInteractions(historicoStatusRepository);
    }

    @Test
    void historico_solicitacaoInexistente_lanca404() {
        when(solicitacaoRepository.existsById(99L)).thenReturn(false);
        assertThrows(RecursoNaoEncontradoException.class, () -> service.historico(99L));
    }

    @Test
    void historico_semMovimentacao_retornaListaVazia() {
        when(solicitacaoRepository.existsById(10L)).thenReturn(true);
        when(historicoStatusRepository.findBySolicitacaoIdOrderByDataMovimentacaoAscIdAsc(10L))
                .thenReturn(List.of());

        assertTrue(service.historico(10L).isEmpty());
    }
}