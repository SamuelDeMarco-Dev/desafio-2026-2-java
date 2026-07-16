package br.com.samuel.documentos_academicos.enums;

import java.util.Set;
import java.util.Arrays;
import java.util.Optional;

public enum CodigoStatus {
    public Set<CodigoStatus> transicoesPermitidas(){
        return switch (this) {
            case ABERTA -> Set.of(EM_ANALISE);
            case EM_ANALISE -> Set.of(APROVADA, REPROVADA);
            case APROVADA -> Set.of(EMITIDA);
            case EMITIDA, REPROVADA -> Set.of();   // finalizados: não saem daqui
        };
    }

    public boolean permiteTransicaoPara(CodigoStatus destino) {
        return transicoesPermitidas().contains(destino);
    }
    
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
