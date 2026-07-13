package br.com.samuel.documentos_academicos.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ErroResponse(
        LocalDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        String path,
        List<CampoErro> campos) {

    public record CampoErro(String campo, String mensagem) {}
}
