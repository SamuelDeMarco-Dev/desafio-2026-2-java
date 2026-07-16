package br.com.samuel.documentos_academicos.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.samuel.documentos_academicos.dto.request.TipoDocumentoRequest;
import br.com.samuel.documentos_academicos.dto.response.TipoDocumentoResponse;
import br.com.samuel.documentos_academicos.entity.TipoDocumento;
import br.com.samuel.documentos_academicos.exception.RecursoDuplicadoException;
import br.com.samuel.documentos_academicos.exception.RecursoNaoEncontradoException;
import br.com.samuel.documentos_academicos.exception.RegraNegocioException;
import br.com.samuel.documentos_academicos.mapper.TipoDocumentoMapper;
import br.com.samuel.documentos_academicos.repository.SolicitacaoRepository;
import br.com.samuel.documentos_academicos.repository.TipoDocumentoRepository;
import br.com.samuel.documentos_academicos.service.TipoDocumentoService;
import br.com.samuel.documentos_academicos.specification.FiltroNomeSpecification;

@Service
@Transactional(readOnly = true)
public class TipoDocumentoServiceImpl implements TipoDocumentoService {

    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final SolicitacaoRepository solicitacaoRepository;
    private final TipoDocumentoMapper tipoDocumentoMapper;

    public TipoDocumentoServiceImpl(TipoDocumentoRepository tipoDocumentoRepository,
                                    SolicitacaoRepository solicitacaoRepository,
                                    TipoDocumentoMapper tipoDocumentoMapper) {
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.tipoDocumentoMapper = tipoDocumentoMapper;
    }

    @Override
    @Transactional
    public TipoDocumentoResponse criar(TipoDocumentoRequest request) {
        if (tipoDocumentoRepository.existsByNomeIgnoreCase(request.nome())) {
            throw new RecursoDuplicadoException("Já existe um tipo de documento com o nome '" + request.nome() + "'");
        }
        TipoDocumento tipo = tipoDocumentoMapper.toEntity(request);
        return tipoDocumentoMapper.toResponse(tipoDocumentoRepository.save(tipo));
    }

    @Override
    public TipoDocumentoResponse buscarPorId(Long id) {
        return tipoDocumentoMapper.toResponse(buscarEntidade(id));
    }

    @Override
    public Page<TipoDocumentoResponse> listar(String nome, Pageable pageable) {
        return tipoDocumentoRepository
                .findAll(FiltroNomeSpecification.<TipoDocumento>contemNome(nome), pageable)
                .map(tipoDocumentoMapper::toResponse);
    }

    @Override
    @Transactional
    public TipoDocumentoResponse atualizar(Long id, TipoDocumentoRequest request) {
        TipoDocumento tipo = buscarEntidade(id);
        if (tipoDocumentoRepository.existsByNomeIgnoreCaseAndIdNot(request.nome(), id)) {
            throw new RecursoDuplicadoException("Já existe um tipo de documento com o nome '" + request.nome() + "'");
        }
        tipo.setNome(request.nome());
        return tipoDocumentoMapper.toResponse(tipoDocumentoRepository.save(tipo));
    }

    @Override
    @Transactional
    public void excluir(Long id) {
        TipoDocumento tipo = buscarEntidade(id);
        if (solicitacaoRepository.existsByTipoDocumentoId(id)) {
            throw new RegraNegocioException(
                "Tipo de documento está vinculado a solicitações e não pode ser removido.");
        }
        tipoDocumentoRepository.delete(tipo);
    }

    private TipoDocumento buscarEntidade(Long id) {
        return tipoDocumentoRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Tipo de documento " + id + " não encontrado"));
    }
}
