package br.com.samuel.documentos_academicos.dto.request;

import java.util.Set;

import br.com.samuel.documentos_academicos.enums.Perfil;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record UsuarioAtualizacaoRequest(
        @NotBlank @Size(max = 150) String nome,
        Integer codigoResponsavel,
        @NotEmpty Set<Perfil> perfis) {
}
