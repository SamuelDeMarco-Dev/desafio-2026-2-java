package br.com.samuel.documentos_academicos.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import br.com.samuel.documentos_academicos.entity.Usuario;
import br.com.samuel.documentos_academicos.exception.CredenciaisInvalidasException;
import br.com.samuel.documentos_academicos.repository.UsuarioRepository;

@Component
public class UsuarioAutenticadoProvider {

    private final UsuarioRepository usuarioRepository;

    public UsuarioAutenticadoProvider(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /** Usuário do token, recarregado do banco (revalida que continua ativo). */
    public Usuario obter() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByLoginAndAtivoTrue(login)
                .orElseThrow(() -> new CredenciaisInvalidasException("Usuário autenticado inválido ou inativo"));
    }
}