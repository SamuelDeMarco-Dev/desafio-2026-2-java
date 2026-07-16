package br.com.samuel.documentos_academicos.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.samuel.documentos_academicos.dto.request.CursoRequest;
import br.com.samuel.documentos_academicos.dto.response.CursoResponse;
import br.com.samuel.documentos_academicos.entity.Curso;
import br.com.samuel.documentos_academicos.exception.RecursoDuplicadoException;
import br.com.samuel.documentos_academicos.exception.RecursoNaoEncontradoException;
import br.com.samuel.documentos_academicos.exception.RegraNegocioException;
import br.com.samuel.documentos_academicos.mapper.CursoMapper;
import br.com.samuel.documentos_academicos.repository.CursoRepository;
import br.com.samuel.documentos_academicos.repository.SolicitacaoRepository;
import br.com.samuel.documentos_academicos.service.CursoService;
import br.com.samuel.documentos_academicos.specification.FiltroNomeSpecification;

@Service
@Transactional(readOnly = true)
public class CursoServiceImpl implements CursoService {

    private final CursoRepository cursoRepository;
    private final SolicitacaoRepository solicitacaoRepository;
    private final CursoMapper cursoMapper;

    public CursoServiceImpl(CursoRepository cursoRepository,
                            SolicitacaoRepository solicitacaoRepository,
                            CursoMapper cursoMapper) {
        this.cursoRepository = cursoRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.cursoMapper = cursoMapper;
    }

    @Override
    @Transactional
    public CursoResponse criar(CursoRequest request) {
        if (cursoRepository.existsByNomeIgnoreCase(request.nome())) {
            throw new RecursoDuplicadoException("Já existe um curso com o nome '" + request.nome() + "'");
        }
        Curso curso = cursoMapper.toEntity(request);
        return cursoMapper.toResponse(cursoRepository.save(curso));
    }

    @Override
    public CursoResponse buscarPorId(Long id) {
        return cursoMapper.toResponse(buscarEntidade(id));
    }

    @Override
    public Page<CursoResponse> listar(String nome, Pageable pageable) {
        return cursoRepository.findAll(FiltroNomeSpecification.<Curso>contemNome(nome), pageable)
                .map(cursoMapper::toResponse);
    }

    @Override
    @Transactional
    public CursoResponse atualizar(Long id, CursoRequest request) {
        Curso curso = buscarEntidade(id);
        if (cursoRepository.existsByNomeIgnoreCaseAndIdNot(request.nome(), id)) {
            throw new RecursoDuplicadoException("Já existe um curso com o nome '" + request.nome() + "'");
        }
        curso.setNome(request.nome());
        return cursoMapper.toResponse(cursoRepository.save(curso));
    }

    @Override
    @Transactional
    public void excluir(Long id) {
        Curso curso = buscarEntidade(id);
        if (solicitacaoRepository.existsByCursoId(id)) {
            throw new RegraNegocioException(
                "Curso está vinculado a solicitações e não pode ser removido.");
        }
        cursoRepository.delete(curso);
    }

    private Curso buscarEntidade(Long id) {
        return cursoRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Curso " + id + " não encontrado"));
    }
}