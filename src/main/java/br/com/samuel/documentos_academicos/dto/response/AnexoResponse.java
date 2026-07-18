package br.com.samuel.documentos_academicos.dto.response;

import java.time.LocalDateTime;

/** Metadados do anexo — o conteúdo binário só sai pelo endpoint de download. */
public record AnexoResponse(
        Long id,
        String nomeArquivo,
        String tipoConteudo,
        long tamanhoBytes,
        LocalDateTime dataUpload) {
}