package br.com.samuel.documentos_academicos.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AlteracaoStatusRequest(
        @NotNull @Positive Long statusId,
        @NotNull Integer codigoResponsavel) {
}