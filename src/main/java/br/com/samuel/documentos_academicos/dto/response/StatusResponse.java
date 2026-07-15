package br.com.samuel.documentos_academicos.dto.response;

public record StatusResponse(
        Long id,
        String codigo,
        String nome,
        Integer responsavel,
        boolean finalizaSolicitacao) {
}