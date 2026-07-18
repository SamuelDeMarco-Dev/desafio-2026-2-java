package br.com.samuel.documentos_academicos.specification;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import br.com.samuel.documentos_academicos.dto.request.SolicitacaoFiltro;
import br.com.samuel.documentos_academicos.entity.Aluno;
import br.com.samuel.documentos_academicos.entity.Curso;
import br.com.samuel.documentos_academicos.entity.Solicitacao;
import br.com.samuel.documentos_academicos.entity.Status;
import br.com.samuel.documentos_academicos.entity.TipoDocumento;
import br.com.samuel.documentos_academicos.enums.Prioridade;
import br.com.samuel.documentos_academicos.repository.AlunoRepository;
import br.com.samuel.documentos_academicos.repository.CursoRepository;
import br.com.samuel.documentos_academicos.repository.SolicitacaoRepository;
import br.com.samuel.documentos_academicos.repository.StatusRepository;
import br.com.samuel.documentos_academicos.repository.TipoDocumentoRepository;

@DataJpaTest
@ActiveProfiles("test")
class SolicitacaoSpecificationTest {

    @Autowired AlunoRepository alunoRepository;
    @Autowired CursoRepository cursoRepository;
    @Autowired TipoDocumentoRepository tipoDocumentoRepository;
    @Autowired StatusRepository statusRepository;
    @Autowired SolicitacaoRepository solicitacaoRepository;

    @BeforeEach
    void seed() {
        Curso direito = cursoRepository.save(curso("Direito"));
        TipoDocumento hist = tipoDocumentoRepository.save(tipo("Histórico"));
        Status aberta = statusRepository.save(status("ABERTA"));
        salvar(alunoRepository.save(aluno("Samuel")), direito, hist, aberta, Prioridade.URGENTE);
        salvar(alunoRepository.save(aluno("Maria")), direito, hist, aberta, Prioridade.NORMAL);
    }

    private Status statusFinalizado() {
        Status s = new Status();
        s.setCodigo("EMITIDA");
        s.setNome("Emitida");
        s.setFinalizaSolicitacao(true);
        return s;
    }

    @Test
    void filtraPorNomeDoAlunoParcialIgnoreCase() {
        var filtro = new SolicitacaoFiltro("sam", null, null, null, null, null, null);
        var page = solicitacaoRepository.findAll(SolicitacaoSpecification.comFiltros(filtro), PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
        assertEquals("Samuel", page.getContent().get(0).getAluno().getNome());
    }

    @Test
    void filtraPorPrioridade() {
        var filtro = new SolicitacaoFiltro(null, null, null, null, Prioridade.URGENTE, null, null);
        var page = solicitacaoRepository.findAll(SolicitacaoSpecification.comFiltros(filtro), PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
    }

    @Test
    void combinaFiltros() {
        var filtro = new SolicitacaoFiltro("maria", "direito", null, "ABERTA", Prioridade.NORMAL, null, null);
        var page = solicitacaoRepository.findAll(SolicitacaoSpecification.comFiltros(filtro), PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
    }

    @Test
    void semFiltrosRetornaTodos() {
        var filtro = new SolicitacaoFiltro(null, null, null, null, null, null, null);
        var page = solicitacaoRepository.findAll(SolicitacaoSpecification.comFiltros(filtro), PageRequest.of(0, 10));
        assertEquals(2, page.getTotalElements());
    }

    @Test
    void semFiltroDeStatus_ocultaSolicitacoesEncerradas() {
        Status emitida = statusRepository.save(statusFinalizado());
        Curso curso = cursoRepository.findAll().get(0);
        TipoDocumento tipo = tipoDocumentoRepository.findAll().get(0);
        salvar(alunoRepository.save(aluno("Pedro")), curso, tipo, emitida, Prioridade.NORMAL);

        var semFiltro = new SolicitacaoFiltro(null, null, null, null, null, null, null);
        var page = solicitacaoRepository.findAll(SolicitacaoSpecification.comFiltros(semFiltro), PageRequest.of(0, 10));
        assertEquals(2, page.getTotalElements(), "a encerrada não deve aparecer sem filtro de status");

        var filtrandoEmitida = new SolicitacaoFiltro(null, null, null, "EMITIDA", null, null, null);
        var pageEmitida = solicitacaoRepository.findAll(SolicitacaoSpecification.comFiltros(filtrandoEmitida),
                PageRequest.of(0, 10));
        assertEquals(1, pageEmitida.getTotalElements(), "filtrando pelo status, a encerrada aparece");
    }

    @Test
    void ordenacaoPadraoEPorPrioridadeDepoisMaisRecente() {
        var filtro = new SolicitacaoFiltro(null, null, null, null, null, null, null);
        var page = solicitacaoRepository.findAll(SolicitacaoSpecification.comFiltros(filtro), PageRequest.of(0, 10));
        assertEquals(Prioridade.URGENTE, page.getContent().get(0).getPrioridade(),
                "URGENTE vem antes de NORMAL na ordenação padrão");
        assertEquals(Prioridade.NORMAL, page.getContent().get(1).getPrioridade());
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

    private Status status(String codigo) {
        Status s = new Status();
        s.setCodigo(codigo);
        s.setNome(codigo);
        s.setFinalizaSolicitacao(false);
        return s;
    }

    private void salvar(Aluno aluno, Curso curso, TipoDocumento tipo, Status status, Prioridade prioridade) {
        Solicitacao s = new Solicitacao();
        s.setAluno(aluno);
        s.setCurso(curso);
        s.setTipoDocumento(tipo);
        s.setStatus(status);
        s.setPrioridade(prioridade);
        LocalDateTime agora = LocalDateTime.now();
        s.setDataSolicitacao(agora);
        s.setDataAlteracao(agora);
        solicitacaoRepository.save(s);
    }
}