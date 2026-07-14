package br.com.samuel.documentos_academicos.dto.request;

import jakarta.validation.constraints.NotNull;

public record AlunoStatusRequest(@NotNull Boolean ativo) {
    
}
