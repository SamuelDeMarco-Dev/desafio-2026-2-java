package br.com.samuel.documentos_academicos.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StatusRequest(
        @NotBlank @Size(max = 30) String codigo,
        @NotBlank @Size(max = 100) String nome,
        Integer responsavel,
        @NotNull Boolean finalizaSolicitacao) {
}