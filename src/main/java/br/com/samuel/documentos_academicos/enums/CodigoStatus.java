package br.com.samuel.documentos_academicos.enums;

import java.util.Arrays;
import java.util.Optional;

public enum CodigoStatus {
    ABERTA(false), 
    EM_ANALISE(false), 
    APROVADA(false), 
    EMITIDA(true), 
    REPROVADA(true);

    private final boolean finalizaSolicitacao;

    CodigoStatus(boolean finalizaSolicitacao){
        this.finalizaSolicitacao = finalizaSolicitacao;
    }

    public boolean finalizaSolicitacao(){
        return finalizaSolicitacao;
    }

    public static Optional<CodigoStatus> porCodigo(String codigo){
        return Arrays.stream(values())
                .filter(c -> c.name().equalsIgnoreCase(codigo))
                .findFirst();
    }
}
