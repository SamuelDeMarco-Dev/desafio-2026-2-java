package br.com.samuel.documentos_academicos.service.impl;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.samuel.documentos_academicos.dto.request.EsqueciSenhaRequest;
import br.com.samuel.documentos_academicos.dto.request.LoginRequest;
import br.com.samuel.documentos_academicos.dto.request.RedefinirSenhaRequest;
import br.com.samuel.documentos_academicos.dto.response.RecuperacaoSenhaResponse;
import br.com.samuel.documentos_academicos.dto.response.TokenResponse;
import br.com.samuel.documentos_academicos.entity.Usuario;
import br.com.samuel.documentos_academicos.exception.CredenciaisInvalidasException;
import br.com.samuel.documentos_academicos.exception.RegraNegocioException;
import br.com.samuel.documentos_academicos.repository.UsuarioRepository;
import br.com.samuel.documentos_academicos.security.JwtService;
import br.com.samuel.documentos_academicos.service.AuthService;

@Service
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    /** Mensagem única: não revela se o login existe ou está inativo. */
    private static final String MENSAGEM_GENERICA = "Login ou senha inválidos";

    private static final String MENSAGEM_RECUPERACAO =
            "Se o login existir, um código de recuperação foi gerado. Ele vale por 15 minutos.";

    private static final String CODIGO_INVALIDO = "Código de recuperação inválido ou expirado";

    private static final int VALIDADE_CODIGO_MINUTOS = 15;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    public AuthServiceImpl(UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           Clock clock) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.clock = clock;
    }

    @Override
    public TokenResponse autenticar(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByLoginAndAtivoTrue(request.login())
                .orElseThrow(() -> new CredenciaisInvalidasException(MENSAGEM_GENERICA));

        if (!passwordEncoder.matches(request.senha(), usuario.getSenha())) {
            throw new CredenciaisInvalidasException(MENSAGEM_GENERICA);
        }

        return new TokenResponse("Bearer",
                jwtService.gerarToken(usuario),
                jwtService.getExpiracaoSegundos());
    }

    /**
     * Gera um código de 6 dígitos com validade curta e guarda apenas o hash.
     * O projeto não tem serviço de e-mail, então o código volta na resposta;
     * em produção o campo viria nulo e o código seguiria por e-mail.
     */
    @Override
    @Transactional
    public RecuperacaoSenhaResponse gerarCodigoRecuperacao(EsqueciSenhaRequest request) {
        Optional<Usuario> encontrado = usuarioRepository.findByLoginAndAtivoTrue(request.login());
        if (encontrado.isEmpty()) {
            return new RecuperacaoSenhaResponse(MENSAGEM_RECUPERACAO, null);
        }

        String codigo = String.format("%06d", random.nextInt(1_000_000));
        Usuario usuario = encontrado.get();
        usuario.setRecuperacaoTokenHash(passwordEncoder.encode(codigo));
        usuario.setRecuperacaoExpiraEm(LocalDateTime.now(clock).plusMinutes(VALIDADE_CODIGO_MINUTOS));
        usuarioRepository.save(usuario);

        return new RecuperacaoSenhaResponse(MENSAGEM_RECUPERACAO, codigo);
    }

    @Override
    @Transactional
    public void redefinirSenha(RedefinirSenhaRequest request) {
        Usuario usuario = usuarioRepository.findByLoginAndAtivoTrue(request.login())
                .orElseThrow(() -> new RegraNegocioException(CODIGO_INVALIDO));

        boolean semCodigoPendente = usuario.getRecuperacaoTokenHash() == null
                || usuario.getRecuperacaoExpiraEm() == null;
        if (semCodigoPendente
                || usuario.getRecuperacaoExpiraEm().isBefore(LocalDateTime.now(clock))
                || !passwordEncoder.matches(request.codigo(), usuario.getRecuperacaoTokenHash())) {
            throw new RegraNegocioException(CODIGO_INVALIDO);
        }

        usuario.setSenha(passwordEncoder.encode(request.novaSenha()));
        usuario.setRecuperacaoTokenHash(null);      // código é de uso único
        usuario.setRecuperacaoExpiraEm(null);
        usuarioRepository.save(usuario);
    }
}