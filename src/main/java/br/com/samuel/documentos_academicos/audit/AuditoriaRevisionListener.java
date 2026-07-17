package br.com.samuel.documentos_academicos.audit;

import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import br.com.samuel.documentos_academicos.entity.AuditoriaRevision;

/**
 * Instanciado pelo Envers por reflexão — não é um bean Spring, então não há
 * injeção de dependência aqui. O SecurityContextHolder é estático, por isso
 * funciona; nunca lançar exceção, sob pena de derrubar a transação auditada.
 */
public class AuditoriaRevisionListener implements RevisionListener {

    private static final String USUARIO_SISTEMA = "sistema";

    @Override
    public void newRevision(Object revisionEntity) {
        AuditoriaRevision revisao = (AuditoriaRevision) revisionEntity;
        revisao.setUsuarioLogin(loginAtual());
    }

    private String loginAtual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return USUARIO_SISTEMA; // ex.: AdminBootstrap no startup, fora de requisição
        }
        return auth.getName();
    }
}
