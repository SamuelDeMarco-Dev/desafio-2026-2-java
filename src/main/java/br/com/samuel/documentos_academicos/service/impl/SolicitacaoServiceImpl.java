package br.com.samuel.documentos_academicos.service.impl;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.samuel.documentos_academicos.dto.request.SolicitacaoCreateRequest;
import br.com.samuel.documentos_academicos.dto.request.SolicitacaoFiltro;
import br.com.samuel.documentos_academicos.dto.response.SolicitacaoResponse;
import br.com.samuel.documentos_academicos.dto.response.SolicitacaoResumoResponse;
import br.com.samuel.documentos_academicos.entity.Aluno;
import br.com.samuel.documentos_academicos.entity.Curso;
import br.com.samuel.documentos_academicos.entity.Solicitacao;
import br.com.samuel.documentos_academicos.entity.Status;
import br.com.samuel.documentos_academicos.entity.TipoDocumento;
import br.com.samuel.documentos_academicos.enums.CodigoStatus;
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
import br.com.samuel.documentos_academicos.specification.SolicitacaoSpecification;

@Service
@Transactional(readOnly = true)
public class SolicitacaoServiceImpl implements SolicitacaoService {

    private final AlunoRepository alunoRepository;
    private final CursoRepository cursoRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final StatusRepository statusRepository;
    private final SolicitacaoRepository solicitacaoRepository;
    private final SolicitacaoMapper solicitacaoMapper;
    private final Clock clock;

    public SolicitacaoServiceImpl(AlunoRepository alunoRepository,
                                  CursoRepository cursoRepository,
                                  TipoDocumentoRepository tipoDocumentoRepository,
                                  StatusRepository statusRepository,
                                  SolicitacaoRepository solicitacaoRepository,
                                  SolicitacaoMapper solicitacaoMapper,
                                  Clock clock) {
        this.alunoRepository = alunoRepository;
        this.cursoRepository = cursoRepository;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.statusRepository = statusRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.solicitacaoMapper = solicitacaoMapper;
        this.clock = clock;
    }

    @Override
    @Transactional
    public SolicitacaoResponse criar(SolicitacaoCreateRequest request) {
        Aluno aluno = alunoRepository.findById(request.alunoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Aluno " + request.alunoId() + " não encontrado"));
        if (!aluno.isAtivo()) {
            throw new AlunoInativoException(
                    "Aluno " + aluno.getId() + " está inativo e não pode solicitar documentos.");
        }

        Curso curso = cursoRepository.findById(request.cursoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Curso " + request.cursoId() + " não encontrado"));

        TipoDocumento tipoDocumento = tipoDocumentoRepository.findById(request.tipoDocumentoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Tipo de documento " + request.tipoDocumentoId() + " não encontrado"));

        Status statusInicial = statusRepository.findByCodigoIgnoreCase(CodigoStatus.ABERTA.name())
                .orElseThrow(() -> new IllegalStateException(
                        "Status inicial ABERTA não encontrado (verifique as migrations)"));

        LocalDateTime agora = LocalDateTime.now(clock);

        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setAluno(aluno);
        solicitacao.setCurso(curso);
        solicitacao.setTipoDocumento(tipoDocumento);
        solicitacao.setStatus(statusInicial);
        solicitacao.setPrioridade(request.prioridade() != null ? request.prioridade() : Prioridade.NORMAL);
        solicitacao.setDataSolicitacao(agora);
        solicitacao.setDataAlteracao(agora);
        // dataEmissao permanece null até a emissão (Issue 19)

        return solicitacaoMapper.toResponse(solicitacaoRepository.save(solicitacao));
    }

@Override
public SolicitacaoResponse buscarPorId(Long id) {
    Solicitacao solicitacao = solicitacaoRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException(
                    "Solicitação " + id + " não encontrada"));
    return solicitacaoMapper.toResponse(solicitacao);
}

@Override
public Page<SolicitacaoResumoResponse> listar(SolicitacaoFiltro filtro, Pageable pageable) {
    return solicitacaoRepository.findAll(SolicitacaoSpecification.comFiltros(filtro), pageable)
            .map(solicitacaoMapper::toResumo);
}

}
