package br.com.samuel.documentos_academicos.dto.request;

import br.com.samuel.documentos_academicos.enums.Prioridade;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SolicitacaoCreateRequest(
        @Schema(description = "Id do aluno; precisa estar ativo", example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @Positive Long alunoId,

        @Schema(description = "Id do curso", example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @Positive Long cursoId,

        @Schema(description = "Id do tipo de documento", example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @Positive Long tipoDocumentoId,

        @Schema(description = "Opcional; assume NORMAL quando omitida", example = "NORMAL")
        Prioridade prioridade) {
}