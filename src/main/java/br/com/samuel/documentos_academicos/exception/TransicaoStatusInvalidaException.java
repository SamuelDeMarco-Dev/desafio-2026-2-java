package br.com.samuel.documentos_academicos.exception;

public class TransicaoStatusInvalidaException extends RegraNegocioException {
    public TransicaoStatusInvalidaException(String mensagem) {
        super(mensagem);
    }
}

