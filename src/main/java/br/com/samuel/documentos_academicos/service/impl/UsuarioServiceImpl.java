package br.com.samuel.documentos_academicos.service.impl;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.samuel.documentos_academicos.dto.request.UsuarioAtualizacaoRequest;
import br.com.samuel.documentos_academicos.dto.request.UsuarioRequest;
import br.com.samuel.documentos_academicos.dto.response.UsuarioResponse;
import br.com.samuel.documentos_academicos.entity.Usuario;
import br.com.samuel.documentos_academicos.enums.Perfil;
import br.com.samuel.documentos_academicos.exception.RecursoDuplicadoException;
import br.com.samuel.documentos_academicos.exception.RecursoNaoEncontradoException;
import br.com.samuel.documentos_academicos.exception.RegraNegocioException;
import br.com.samuel.documentos_academicos.mapper.UsuarioMapper;
import br.com.samuel.documentos_academicos.repository.UsuarioRepository;
import br.com.samuel.documentos_academicos.service.UsuarioService;
import br.com.samuel.documentos_academicos.specification.UsuarioSpecification;

@Service
@Transactional(readOnly = true)
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
                              UsuarioMapper usuarioMapper,
                              PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UsuarioResponse criar(UsuarioRequest request) {
        if (usuarioRepository.existsByLogin(request.login())) {
            throw new RecursoDuplicadoException(
                    "Já existe um usuário com o login '" + request.login() + "'");
        }
        if (request.codigoResponsavel() != null
                && usuarioRepository.existsByCodigoResponsavel(request.codigoResponsavel())) {
            throw new RecursoDuplicadoException(
                    "Já existe um usuário com o código de responsável " + request.codigoResponsavel());
        }
        exigirCodigoParaQuemMovimenta(request.perfis(), request.codigoResponsavel());

        Usuario usuario = new Usuario();
        usuario.setNome(request.nome());
        usuario.setLogin(request.login());
        usuario.setSenha(passwordEncoder.encode(request.senha()));
        usuario.setCodigoResponsavel(request.codigoResponsavel());
        usuario.setAtivo(true);
        usuario.setPerfis(new HashSet<>(request.perfis()));

        return usuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    @Override
    public UsuarioResponse buscarPorId(Long id) {
        return usuarioMapper.toResponse(buscarEntidade(id));
    }

    @Override
    public Page<UsuarioResponse> listar(String nome, Boolean ativo, Pageable pageable) {
        return usuarioRepository.findAll(UsuarioSpecification.comFiltros(nome, ativo), pageable)
                .map(usuarioMapper::toResponse);
    }

    @Override
    @Transactional
    public UsuarioResponse atualizar(Long id, UsuarioAtualizacaoRequest request) {
        Usuario usuario = buscarEntidade(id);
        if (request.codigoResponsavel() != null
                && !request.codigoResponsavel().equals(usuario.getCodigoResponsavel())
                && usuarioRepository.existsByCodigoResponsavel(request.codigoResponsavel())) {
            throw new RecursoDuplicadoException(
                    "Já existe um usuário com o código de responsável " + request.codigoResponsavel());
        }
        exigirCodigoParaQuemMovimenta(request.perfis(), request.codigoResponsavel());
        usuario.setNome(request.nome());
        usuario.setCodigoResponsavel(request.codigoResponsavel());
        usuario.setPerfis(new HashSet<>(request.perfis()));
        return usuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    /**
     * Quem tem perfil ADMIN ou OPERADOR movimenta solicitações, e a movimentação
     * exige um código de responsável (que fica registrado no histórico). Sem ele,
     * o usuário só descobriria o problema — com um erro genérico — na hora de
     * movimentar. Barramos já no cadastro, com uma mensagem clara.
     */
    private void exigirCodigoParaQuemMovimenta(Set<Perfil> perfis, Integer codigoResponsavel) {
        boolean movimentaSolicitacoes = perfis.contains(Perfil.ADMIN) || perfis.contains(Perfil.OPERADOR);
        if (movimentaSolicitacoes && codigoResponsavel == null) {
            throw new RegraNegocioException(
                    "Usuários com perfil ADMIN ou OPERADOR precisam de um código de responsável, "
                    + "pois ele identifica quem movimenta as solicitações.");
        }
    }

    @Override
    @Transactional
    public UsuarioResponse alterarSituacao(Long id, boolean ativo) {
        Usuario usuario = buscarEntidade(id);
        usuario.setAtivo(ativo);
        return usuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    private Usuario buscarEntidade(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário " + id + " não encontrado"));
    }
}
