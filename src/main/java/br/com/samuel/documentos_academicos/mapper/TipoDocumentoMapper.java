package br.com.samuel.documentos_academicos.mapper;

import org.springframework.stereotype.Component;

import br.com.samuel.documentos_academicos.dto.request.TipoDocumentoRequest;
import br.com.samuel.documentos_academicos.dto.response.TipoDocumentoResponse;
import br.com.samuel.documentos_academicos.entity.TipoDocumento;

@Component
public class TipoDocumentoMapper {

    public TipoDocumento toEntity(TipoDocumentoRequest req) {
        TipoDocumento tipo = new TipoDocumento();
        tipo.setNome(req.nome());
        return tipo;
    }

    public TipoDocumentoResponse toResponse(TipoDocumento tipo) {
        return new TipoDocumentoResponse(tipo.getId(), tipo.getNome());
    }
}