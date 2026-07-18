package br.com.samuel.documentos_academicos.dto.response;

/**
 * O código só é devolvido na resposta porque o projeto não possui serviço de
 * e-mail: em produção este campo seria sempre nulo e o código chegaria por
 * um canal privado (e-mail/SMS do usuário).
 */
public record RecuperacaoSenhaResponse(String mensagem, String codigoRecuperacao) {
}