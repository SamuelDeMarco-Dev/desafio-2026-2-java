package br.com.samuel.documentos_academicos.dto.request;

import jakarta.validation.constraints.NotBlank;

public record EsqueciSenhaRequest(@NotBlank String login) {
}