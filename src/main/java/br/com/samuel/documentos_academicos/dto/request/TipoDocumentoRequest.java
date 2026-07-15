package br.com.samuel.documentos_academicos.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TipoDocumentoRequest(
    @NotBlank
    @Size(max = 150)
    String nome) {
    
}
