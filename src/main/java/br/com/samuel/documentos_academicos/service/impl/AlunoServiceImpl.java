package br.com.samuel.documentos_academicos.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.samuel.documentos_academicos.dto.request.AlunoRequest;
import br.com.samuel.documentos_academicos.dto.response.AlunoResponse;
import br.com.samuel.documentos_academicos.entity.Aluno;
import br.com.samuel.documentos_academicos.exception.RecursoNaoEncontradoException;
import br.com.samuel.documentos_academicos.exception.RegraNegocioException;
import br.com.samuel.documentos_academicos.mapper.AlunoMapper;
import br.com.samuel.documentos_academicos.repository.AlunoRepository;
import br.com.samuel.documentos_academicos.repository.SolicitacaoRepository;
import br.com.samuel.documentos_academicos.service.AlunoService;

@Service
@Transactional(readOnly = true)
public class AlunoServiceImpl implements AlunoService {

    private final AlunoRepository alunoRepository;
    private final SolicitacaoRepository solicitacaoRepository;
    private final AlunoMapper alunoMapper;

    public AlunoServiceImpl(AlunoRepository alunoRepository,
                            SolicitacaoRepository solicitacaoRepository,
                            AlunoMapper alunoMapper) {
        this.alunoRepository = alunoRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.alunoMapper = alunoMapper;
    }

    @Override
    @Transactional
    public AlunoResponse criar(AlunoRequest request) {
        Aluno aluno = alunoMapper.toEntity(request); // ativo = true
        return alunoMapper.toResponse(alunoRepository.save(aluno));
    }

    @Override
    public AlunoResponse buscarPorId(Long id) {
        return alunoMapper.toResponse(buscarEntidade(id));
    }

    @Override
    public Page<AlunoResponse> listar(String nome, Boolean ativo, Pageable pageable) {
        return alunoRepository.buscar(nome, ativo, pageable).map(alunoMapper::toResponse);
    }

    @Override
    @Transactional
    public AlunoResponse atualizar(Long id, AlunoRequest request) {
        Aluno aluno = buscarEntidade(id);
        aluno.setNome(request.nome());
        return alunoMapper.toResponse(alunoRepository.save(aluno));
    }

    @Override
    @Transactional
    public AlunoResponse alterarSituacao(Long id, boolean ativo) {
        Aluno aluno = buscarEntidade(id);
        aluno.setAtivo(ativo);
        return alunoMapper.toResponse(alunoRepository.save(aluno));
    }

    @Override
    @Transactional
    public void excluir(Long id) {
        Aluno aluno = buscarEntidade(id);
        if (solicitacaoRepository.existsByAlunoId(id)) {
            throw new RegraNegocioException(
                "Aluno possui solicitações e não pode ser removido. Utilize a inativação.");
        }
        alunoRepository.delete(aluno);
    }

    private Aluno buscarEntidade(Long id) {
        return alunoRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno " + id + " não encontrado"));
    }
}