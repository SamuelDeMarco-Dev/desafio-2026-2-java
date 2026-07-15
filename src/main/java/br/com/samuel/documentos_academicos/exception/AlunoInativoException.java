package br.com.samuel.documentos_academicos.exception;

public class AlunoInativoException extends RegraNegocioException {
    public AlunoInativoException(String mensagem){
        super(mensagem);
    }
}
