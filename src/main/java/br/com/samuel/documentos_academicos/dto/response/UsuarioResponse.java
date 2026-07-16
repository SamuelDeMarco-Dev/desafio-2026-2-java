package br.com.samuel.documentos_academicos.dto.response;

import java.util.Set;

import br.com.samuel.documentos_academicos.enums.Perfil;

/** Contrato de saída de usuário — sem o campo senha, por design. */
public record UsuarioResponse(
        Long id,
        String nome,
        String login,
        Integer codigoResponsavel,
        boolean ativo,
        Set<Perfil> perfis) {
}