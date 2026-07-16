package br.com.samuel.documentos_academicos.mapper;

import org.springframework.stereotype.Component;

import br.com.samuel.documentos_academicos.dto.response.HistoricoStatusResponse;
import br.com.samuel.documentos_academicos.dto.response.ResponsavelResponse;
import br.com.samuel.documentos_academicos.dto.response.StatusResponse;
import br.com.samuel.documentos_academicos.entity.HistoricoStatus;
import br.com.samuel.documentos_academicos.entity.Status;

@Component
public class HistoricoStatusMapper {

    public HistoricoStatusResponse toResponse(HistoricoStatus h) {
        return new HistoricoStatusResponse(
                h.getId(),
                h.getStatusAnterior() != null ? toStatus(h.getStatusAnterior()) : null,
                toStatus(h.getStatusNovo()),
                new ResponsavelResponse(h.getUsuario().getId(), h.getUsuario().getNome(),
                                        h.getUsuario().getCodigoResponsavel()),
                h.getDataMovimentacao());
    }

    private StatusResponse toStatus(Status s) {
        return new StatusResponse(s.getId(), s.getCodigo(), s.getNome(),
                                  s.getResponsavel(), s.isFinalizaSolicitacao());
    }
}