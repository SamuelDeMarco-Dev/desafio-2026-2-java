package br.com.samuel.documentos_academicos.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.samuel.documentos_academicos.dto.request.StatusRequest;
import br.com.samuel.documentos_academicos.dto.response.StatusResponse;
import br.com.samuel.documentos_academicos.entity.Status;
import br.com.samuel.documentos_academicos.enums.CodigoStatus;
import br.com.samuel.documentos_academicos.exception.RecursoDuplicadoException;
import br.com.samuel.documentos_academicos.exception.RecursoNaoEncontradoException;
import br.com.samuel.documentos_academicos.exception.RegraNegocioException;
import br.com.samuel.documentos_academicos.mapper.StatusMapper;
import br.com.samuel.documentos_academicos.repository.SolicitacaoRepository;
import br.com.samuel.documentos_academicos.repository.StatusRepository;
import br.com.samuel.documentos_academicos.service.StatusService;

@Service
@Transactional(readOnly = true)
public class StatusServiceImpl implements StatusService {

    private final StatusRepository statusRepository;
    private final SolicitacaoRepository solicitacaoRepository;
    private final StatusMapper statusMapper;

    public StatusServiceImpl(StatusRepository statusRepository,
                             SolicitacaoRepository solicitacaoRepository,
                             StatusMapper statusMapper) {
        this.statusRepository = statusRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.statusMapper = statusMapper;
    }

    @Override
    @Transactional
    public StatusResponse criar(StatusRequest request) {
        if (statusRepository.existsByCodigoIgnoreCase(request.codigo())) {
            throw new RecursoDuplicadoException("Já existe um status com o código '" + request.codigo() + "'");
        }
        validarConsistenciaFinalizacao(request.codigo(), request.finalizaSolicitacao());
        // TODO Issue 16: validar que 'responsavel' corresponde a um usuário ativo
        Status status = statusMapper.toEntity(request);
        return statusMapper.toResponse(statusRepository.save(status));
    }

    @Override
    public StatusResponse buscarPorId(Long id) {
        return statusMapper.toResponse(buscarEntidade(id));
    }

    @Override
    public List<StatusResponse> listar() {
        return statusRepository.findAllByOrderByIdAsc().stream()
                .map(statusMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public StatusResponse atualizar(Long id, StatusRequest request) {
        Status status = buscarEntidade(id);
        Optional<CodigoStatus> estrutural = CodigoStatus.porCodigo(status.getCodigo());

        if (estrutural.isPresent()) {
            // status estrutural: código e finalização são imutáveis
            if (!status.getCodigo().equalsIgnoreCase(request.codigo())) {
                throw new RegraNegocioException("Não é permitido alterar o código de um status estrutural.");
            }
            if (estrutural.get().finalizaSolicitacao() != Boolean.TRUE.equals(request.finalizaSolicitacao())) {
                throw new RegraNegocioException("O status '" + status.getCodigo()
                        + "' deve manter finalizaSolicitacao=" + estrutural.get().finalizaSolicitacao());
            }
        } else {
            // status customizado: código pode mudar, mas continua único
            if (statusRepository.existsByCodigoIgnoreCaseAndIdNot(request.codigo(), id)) {
                throw new RecursoDuplicadoException("Já existe um status com o código '" + request.codigo() + "'");
            }
            status.setCodigo(request.codigo());
            status.setFinalizaSolicitacao(Boolean.TRUE.equals(request.finalizaSolicitacao()));
        }
        // TODO Issue 16: validar 'responsavel' como usuário ativo
        status.setNome(request.nome());
        status.setResponsavel(request.responsavel());
        return statusMapper.toResponse(statusRepository.save(status));
    }

    @Override
    @Transactional
    public void excluir(Long id) {
        Status status = buscarEntidade(id);
        if (CodigoStatus.porCodigo(status.getCodigo()).isPresent()) {
            throw new RegraNegocioException("Status estrutural do fluxo não pode ser removido.");
        }
        if (solicitacaoRepository.existsByStatusId(id)) {
            throw new RegraNegocioException("Status está vinculado a solicitações e não pode ser removido.");
        }
        statusRepository.delete(status);
    }

    private void validarConsistenciaFinalizacao(String codigo, Boolean finaliza) {
        CodigoStatus.porCodigo(codigo).ifPresent(estrutural -> {
            if (estrutural.finalizaSolicitacao() != Boolean.TRUE.equals(finaliza)) {
                throw new RegraNegocioException("O status '" + codigo
                        + "' deve ter finalizaSolicitacao=" + estrutural.finalizaSolicitacao());
            }
        });
    }

    private Status buscarEntidade(Long id) {
        return statusRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Status " + id + " não encontrado"));
    }
}
