package br.com.samuel.documentos_academicos.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import br.com.samuel.documentos_academicos.dto.response.ContagemStatusResponse;
import br.com.samuel.documentos_academicos.entity.Aluno;
import br.com.samuel.documentos_academicos.entity.Curso;
import br.com.samuel.documentos_academicos.entity.Solicitacao;
import br.com.samuel.documentos_academicos.entity.Status;
import br.com.samuel.documentos_academicos.entity.TipoDocumento;
import br.com.samuel.documentos_academicos.enums.Prioridade;

@DataJpaTest
@ActiveProfiles("test")
class DashboardQueriesTest {

    @Autowired AlunoRepository alunoRepository;
    @Autowired CursoRepository cursoRepository;
    @Autowired TipoDocumentoRepository tipoDocumentoRepository;
    @Autowired StatusRepository statusRepository;
    @Autowired SolicitacaoRepository solicitacaoRepository;

    @BeforeEach
    void seed() {
        Curso curso = cursoRepository.save(curso("Direito"));
        TipoDocumento historico = tipoDocumentoRepository.save(tipo("Histórico"));
        TipoDocumento diploma = tipoDocumentoRepository.save(tipo("Diploma"));
        Status aberta = statusRepository.save(status("ABERTA", false));
        Status emitida = statusRepository.save(status("EMITIDA", true));
        Aluno samuel = alunoRepository.save(aluno("Samuel"));

        // 2 de Histórico (uma emitida em 2 dias), 1 de Diploma (não emitida)
        salvar(samuel, curso, historico, emitida, 2);
        salvar(samuel, curso, historico, aberta, null);
        salvar(samuel, curso, diploma, aberta, null);
    }

    @Test
    void contaPorStatus() {
        var lista = solicitacaoRepository.contarPorStatus(null, null);
        assertEquals(3, lista.stream().mapToLong(ContagemStatusResponse::total).sum());
    }

    @Test
    void documentosMaisSolicitadosOrdenaDesc() {
        var lista = solicitacaoRepository.documentosMaisSolicitados(null, null);
        assertEquals("Histórico", lista.get(0).tipoDocumento());
        assertEquals(2, lista.get(0).total());
    }

    @Test
    void intervalosEmissaoSoConsideraEmitidas() {
        assertEquals(1, solicitacaoRepository.intervalosEmissao(null, null).size());
    }

    // ----- helpers -----

    private Aluno aluno(String nome) {
        Aluno a = new Aluno();
        a.setNome(nome);
        a.setAtivo(true);
        return a;
    }

    private Curso curso(String nome) {
        Curso c = new Curso();
        c.setNome(nome);
        return c;
    }

    private TipoDocumento tipo(String nome) {
        TipoDocumento t = new TipoDocumento();
        t.setNome(nome);
        return t;
    }

    private Status status(String codigo, boolean finaliza) {
        Status s = new Status();
        s.setCodigo(codigo);
        s.setNome(codigo);
        s.setFinalizaSolicitacao(finaliza);
        return s;
    }

    private void salvar(Aluno aluno, Curso curso, TipoDocumento tipo, Status status, Integer diasEmissao) {
        Solicitacao s = new Solicitacao();
        s.setAluno(aluno);
        s.setCurso(curso);
        s.setTipoDocumento(tipo);
        s.setStatus(status);
        s.setPrioridade(Prioridade.NORMAL);
        LocalDateTime agora = LocalDateTime.now();
        s.setDataSolicitacao(agora);
        s.setDataAlteracao(agora);
        if (diasEmissao != null) {
            s.setDataEmissao(agora.plusDays(diasEmissao));
        }
        solicitacaoRepository.save(s);
    }
}