package br.com.samuel.documentos_academicos.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import br.com.samuel.documentos_academicos.entity.Aluno;
import br.com.samuel.documentos_academicos.entity.Curso;
import br.com.samuel.documentos_academicos.entity.HistoricoStatus;
import br.com.samuel.documentos_academicos.entity.Solicitacao;
import br.com.samuel.documentos_academicos.entity.Status;
import br.com.samuel.documentos_academicos.entity.TipoDocumento;
import br.com.samuel.documentos_academicos.entity.Usuario;
import br.com.samuel.documentos_academicos.enums.Perfil;
import br.com.samuel.documentos_academicos.enums.Prioridade;

@DataJpaTest
@ActiveProfiles("test")
class HistoricoStatusRepositoryTest {

    @Autowired AlunoRepository alunoRepository;
    @Autowired CursoRepository cursoRepository;
    @Autowired TipoDocumentoRepository tipoDocumentoRepository;
    @Autowired StatusRepository statusRepository;
    @Autowired SolicitacaoRepository solicitacaoRepository;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired HistoricoStatusRepository historicoStatusRepository;

    /**
     * As três movimentações compartilham o mesmo timestamp de propósito: é o cenário
     * real dos testes com Clock fixo, e é o que exige o desempate por id na query.
     */
    @Test
    void retornaMovimentacoesNaOrdemDeInsercaoQuandoTimestampsEmpatam() {
        Status aberta = statusRepository.save(status("ABERTA", false));
        Status emAnalise = statusRepository.save(status("EM_ANALISE", false));
        Status aprovada = statusRepository.save(status("APROVADA", false));
        Status emitida = statusRepository.save(status("EMITIDA", true));
        Usuario usuario = usuarioRepository.save(usuario());
        Solicitacao s = solicitacaoRepository.save(solicitacao(aberta));

        LocalDateTime mesmoInstante = LocalDateTime.of(2026, 7, 16, 10, 0);
        salvar(s, null, aberta, usuario, mesmoInstante);
        salvar(s, aberta, emAnalise, usuario, mesmoInstante);
        salvar(s, emAnalise, aprovada, usuario, mesmoInstante);
        salvar(s, aprovada, emitida, usuario, mesmoInstante);

        List<HistoricoStatus> historico =
                historicoStatusRepository.findBySolicitacaoIdOrderByDataMovimentacaoAscIdAsc(s.getId());

        assertEquals(4, historico.size());
        assertNull(historico.get(0).getStatusAnterior(), "a primeira linha é a abertura");
        assertEquals("ABERTA", historico.get(0).getStatusNovo().getCodigo());
        assertEquals("EM_ANALISE", historico.get(1).getStatusNovo().getCodigo());
        assertEquals("APROVADA", historico.get(2).getStatusNovo().getCodigo());
        assertEquals("EMITIDA", historico.get(3).getStatusNovo().getCodigo());
    }

    @Test
    void naoTrazHistoricoDeOutraSolicitacao() {
        Status aberta = statusRepository.save(status("ABERTA", false));
        Usuario usuario = usuarioRepository.save(usuario());
        Solicitacao alvo = solicitacaoRepository.save(solicitacao(aberta));
        Solicitacao outra = solicitacaoRepository.save(solicitacao(aberta));

        LocalDateTime agora = LocalDateTime.of(2026, 7, 16, 10, 0);
        salvar(alvo, null, aberta, usuario, agora);
        salvar(outra, null, aberta, usuario, agora);

        assertEquals(1, historicoStatusRepository
                .findBySolicitacaoIdOrderByDataMovimentacaoAscIdAsc(alvo.getId()).size());
    }

    // ----- helpers -----

    private Status status(String codigo, boolean finaliza) {
        Status s = new Status();
        s.setCodigo(codigo);
        s.setNome(codigo);
        s.setFinalizaSolicitacao(finaliza);
        return s;
    }

    private Usuario usuario() {
        Usuario u = new Usuario();
        u.setNome("Administrador");
        u.setLogin("administrador");
        u.setSenha("$2a$10$hashfalsoparateste");
        u.setCodigoResponsavel(1000);
        u.setAtivo(true);
        u.getPerfis().add(Perfil.ADMIN);
        return u;
    }

    private Solicitacao solicitacao(Status status) {
        Aluno a = new Aluno();
        a.setNome("Samuel");
        a.setAtivo(true);
        alunoRepository.save(a);

        Curso c = new Curso();
        c.setNome("Direito " + System.nanoTime());
        cursoRepository.save(c);

        TipoDocumento t = new TipoDocumento();
        t.setNome("Historico " + System.nanoTime());
        tipoDocumentoRepository.save(t);

        Solicitacao s = new Solicitacao();
        s.setAluno(a);
        s.setCurso(c);
        s.setTipoDocumento(t);
        s.setStatus(status);
        s.setPrioridade(Prioridade.NORMAL);
        LocalDateTime agora = LocalDateTime.of(2026, 7, 16, 9, 0);
        s.setDataSolicitacao(agora);
        s.setDataAlteracao(agora);
        return s;
    }

    private void salvar(Solicitacao s, Status anterior, Status novo, Usuario u, LocalDateTime quando) {
        HistoricoStatus h = new HistoricoStatus();
        h.setSolicitacao(s);
        h.setStatusAnterior(anterior);
        h.setStatusNovo(novo);
        h.setUsuario(u);
        h.setDataMovimentacao(quando);
        historicoStatusRepository.save(h);
    }
}