package br.com.samuel.documentos_academicos.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RedefinirSenhaRequest(
        @NotBlank String login,
        @NotBlank String codigo,
        @NotBlank @Size(min = 8, max = 72) String novaSenha) {
}