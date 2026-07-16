package br.com.samuel.documentos_academicos.dto.response;

public record TokenResponse(String tipo, String token, long expiraEm) {
}