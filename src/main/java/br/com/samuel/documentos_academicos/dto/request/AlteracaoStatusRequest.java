package br.com.samuel.documentos_academicos.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AlteracaoStatusRequest(
        @Schema(description = "Id do status de destino; a transição precisa estar prevista no fluxo",
                example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @Positive Long statusId,

        @Schema(description = "Código do responsável; precisa ser o do usuário autenticado",
                example = "1000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull Integer codigoResponsavel) {
}