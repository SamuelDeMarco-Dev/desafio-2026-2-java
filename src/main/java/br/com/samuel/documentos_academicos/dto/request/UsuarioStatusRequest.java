package br.com.samuel.documentos_academicos.dto.request;

import jakarta.validation.constraints.NotNull;

public record UsuarioStatusRequest(@NotNull Boolean ativo) {
}
