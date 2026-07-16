package br.com.samuel.documentos_academicos.enums;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

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

    public static Optional<CodigoStatus> porCodigo(String codigo){
        return Arrays.stream(values())
                .filter(c -> c.name().equalsIgnoreCase(codigo))
                .findFirst();
    }
}