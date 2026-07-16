package br.com.samuel.documentos_academicos.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.samuel.documentos_academicos.dto.request.LoginRequest;
import br.com.samuel.documentos_academicos.dto.response.TokenResponse;
import br.com.samuel.documentos_academicos.entity.Usuario;
import br.com.samuel.documentos_academicos.exception.CredenciaisInvalidasException;
import br.com.samuel.documentos_academicos.repository.UsuarioRepository;
import br.com.samuel.documentos_academicos.security.JwtService;
import br.com.samuel.documentos_academicos.service.AuthService;

@Service
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    /** Mensagem única: não revela se o login existe ou está inativo. */
    private static final String MENSAGEM_GENERICA = "Login ou senha inválidos";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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
}