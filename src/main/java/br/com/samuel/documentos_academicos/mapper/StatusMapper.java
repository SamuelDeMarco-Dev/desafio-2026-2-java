package br.com.samuel.documentos_academicos.mapper;

import org.springframework.stereotype.Component;

import br.com.samuel.documentos_academicos.dto.request.StatusRequest;
import br.com.samuel.documentos_academicos.dto.response.StatusResponse;
import br.com.samuel.documentos_academicos.entity.Status;

@Component
public class StatusMapper {

    public Status toEntity(StatusRequest req) {
        Status status = new Status();
        status.setCodigo(req.codigo());
        status.setNome(req.nome());
        status.setResponsavel(req.responsavel());
        status.setFinalizaSolicitacao(Boolean.TRUE.equals(req.finalizaSolicitacao()));
        return status;
    }

    public StatusResponse toResponse(Status status) {
        return new StatusResponse(
                status.getId(),
                status.getCodigo(),
                status.getNome(),
                status.getResponsavel(),
                status.isFinalizaSolicitacao());
    }
}