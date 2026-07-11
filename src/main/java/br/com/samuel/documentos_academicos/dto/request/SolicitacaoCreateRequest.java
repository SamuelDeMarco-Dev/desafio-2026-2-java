package br.com.samuel.documentos_academicos.dto.request;

import br.com.samuel.documentos_academicos.enums.Prioridade;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SolicitacaoCreateRequest(
        @NotNull @Positive Long alunoId,
        @NotNull @Positive Long cursoId,
        @NotNull @Positive Long tipoDocumentoId,
        Prioridade prioridade) {
}