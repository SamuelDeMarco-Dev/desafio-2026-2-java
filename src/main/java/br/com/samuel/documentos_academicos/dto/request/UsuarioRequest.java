package br.com.samuel.documentos_academicos.dto.request;

import java.util.Set;

import br.com.samuel.documentos_academicos.enums.Perfil;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank @Size(max = 150) String nome,
        @NotBlank @Size(max = 50) String login,
        @NotBlank @Size(min = 8, max = 72) String senha,
        Integer codigoResponsavel,
        @NotEmpty Set<Perfil> perfis) {
}