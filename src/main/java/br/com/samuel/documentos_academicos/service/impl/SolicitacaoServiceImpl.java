package br.com.samuel.documentos_academicos.service.impl;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.samuel.documentos_academicos.dto.request.AlteracaoStatusRequest;
import br.com.samuel.documentos_academicos.dto.request.SolicitacaoCreateRequest;
import br.com.samuel.documentos_academicos.dto.request.SolicitacaoFiltro;
import br.com.samuel.documentos_academicos.dto.response.SolicitacaoResponse;
import br.com.samuel.documentos_academicos.dto.response.SolicitacaoResumoResponse;
import br.com.samuel.documentos_academicos.entity.Aluno;
import br.com.samuel.documentos_academicos.entity.Curso;
import br.com.samuel.documentos_academicos.entity.Solicitacao;
import br.com.samuel.documentos_academicos.entity.Status;
import br.com.samuel.documentos_academicos.entity.TipoDocumento;
import br.com.samuel.documentos_academicos.entity.Usuario;
import br.com.samuel.documentos_academicos.enums.CodigoStatus;
import br.com.samuel.documentos_academicos.enums.Prioridade;
import br.com.samuel.documentos_academicos.exception.AlunoInativoException;
import br.com.samuel.documentos_academicos.exception.RecursoNaoEncontradoException;
import br.com.samuel.documentos_academicos.exception.RegraNegocioException;
import br.com.samuel.documentos_academicos.exception.ResponsavelInvalidoException;
import br.com.samuel.documentos_academicos.exception.TransicaoStatusInvalidaException;
import br.com.samuel.documentos_academicos.mapper.SolicitacaoMapper;
import br.com.samuel.documentos_academicos.repository.AlunoRepository;
import br.com.samuel.documentos_academicos.repository.CursoRepository;
import br.com.samuel.documentos_academicos.repository.SolicitacaoRepository;
import br.com.samuel.documentos_academicos.repository.StatusRepository;
import br.com.samuel.documentos_academicos.repository.TipoDocumentoRepository;
import br.com.samuel.documentos_academicos.security.UsuarioAutenticadoProvider;
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
    private final UsuarioAutenticadoProvider usuarioAutenticadoProvider;
    private final Clock clock;

    public SolicitacaoServiceImpl(AlunoRepository alunoRepository,
                                  CursoRepository cursoRepository,
                                  TipoDocumentoRepository tipoDocumentoRepository,
                                  StatusRepository statusRepository,
                                  SolicitacaoRepository solicitacaoRepository,
                                  SolicitacaoMapper solicitacaoMapper,
                                  UsuarioAutenticadoProvider usuarioAutenticadoProvider,
                                  Clock clock) {
        this.alunoRepository = alunoRepository;
        this.cursoRepository = cursoRepository;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.statusRepository = statusRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.solicitacaoMapper = solicitacaoMapper;
        this.usuarioAutenticadoProvider = usuarioAutenticadoProvider;
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

@Override
public Page<SolicitacaoResumoResponse> listarPorAluno(Long alunoId, Pageable pageable) {
    if (!alunoRepository.existsById(alunoId)) {
        throw new RecursoNaoEncontradoException("Aluno " + alunoId + " não encontrado");
    }
    return solicitacaoRepository.findByAlunoId(alunoId, pageable)
            .map(solicitacaoMapper::toResumo);
}

@Override
@Transactional
public SolicitacaoResponse alterarStatus(Long id, AlteracaoStatusRequest request) {
    Solicitacao solicitacao = solicitacaoRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Solicitação " + id + " não encontrada"));
    Status destino = statusRepository.findById(request.statusId())
            .orElseThrow(() -> new RecursoNaoEncontradoException(
                    "Status " + request.statusId() + " não encontrado"));

    Usuario autenticado = usuarioAutenticadoProvider.obter();

    // 1) o código informado tem que ser o do próprio usuário autenticado
    if (!Objects.equals(request.codigoResponsavel(), autenticado.getCodigoResponsavel())) {
        throw new ResponsavelInvalidoException(
                "O código de responsável informado não corresponde ao usuário autenticado");
    }

    // 2) solicitação finalizada não se movimenta
    Status atual = solicitacao.getStatus();
    if (atual.isFinalizaSolicitacao()) {
        throw new RegraNegocioException(
                "Solicitação finalizada em '" + atual.getCodigo() + "' não pode ser movimentada");
    }

    // 3) a transição precisa estar prevista no fluxo
    CodigoStatus origem = CodigoStatus.porCodigo(atual.getCodigo())
            .orElseThrow(() -> new RegraNegocioException("Status atual não pertence ao fluxo"));
    CodigoStatus alvo = CodigoStatus.porCodigo(destino.getCodigo())
            .orElseThrow(() -> new TransicaoStatusInvalidaException(
                    "Status de destino não pertence ao fluxo"));
    if (!origem.permiteTransicaoPara(alvo)) {
        throw new TransicaoStatusInvalidaException(
                "A transição de " + origem + " para " + alvo + " não é permitida");
    }

    // 4) quando o status tem responsável definido, só ele movimenta
    if (destino.getResponsavel() != null
            && !Objects.equals(destino.getResponsavel(), autenticado.getCodigoResponsavel())) {
        throw new ResponsavelInvalidoException(
                "Apenas o responsável pelo status '" + destino.getCodigo() + "' pode realizar esta movimentação");
    }

    // 5) aplica
    LocalDateTime agora = LocalDateTime.now(clock);
    solicitacao.setStatus(destino);
    solicitacao.setDataAlteracao(agora);
    if (alvo == CodigoStatus.EMITIDA) {
        solicitacao.setDataEmissao(agora);   // REPROVADA mantém dataEmissao nula
    }

    return solicitacaoMapper.toResponse(solicitacaoRepository.save(solicitacao));
}


}
