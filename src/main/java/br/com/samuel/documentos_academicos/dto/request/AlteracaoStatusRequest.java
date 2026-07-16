package br.com.samuel.documentos_academicos.dto.request;

public record AlteracaoStatusRequest(
        @NotNull @Positive Long statusId,
        @NotNull Integer codigoResponsavel) {
}

