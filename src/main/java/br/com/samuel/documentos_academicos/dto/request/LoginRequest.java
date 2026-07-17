package br.com.samuel.documentos_academicos.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(description = "Login do usuário", example = "administrador",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String login,

        @Schema(description = "Senha em texto puro; nunca é devolvida em nenhuma resposta",
                example = "sua-senha-aqui", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String senha) {
}