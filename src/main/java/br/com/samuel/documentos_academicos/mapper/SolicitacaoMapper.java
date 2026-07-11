package br.com.samuel.documentos_academicos.mapper;

import org.springframework.stereotype.Component;

import br.com.samuel.documentos_academicos.dto.response.AlunoResponse;
import br.com.samuel.documentos_academicos.dto.response.CursoResponse;
import br.com.samuel.documentos_academicos.dto.response.SolicitacaoResponse;
import br.com.samuel.documentos_academicos.dto.response.SolicitacaoResumoResponse;
import br.com.samuel.documentos_academicos.dto.response.StatusResponse;
import br.com.samuel.documentos_academicos.dto.response.TipoDocumentoResponse;
import br.com.samuel.documentos_academicos.entity.Solicitacao;

@Component
public class SolicitacaoMapper {

    public SolicitacaoResponse toResponse(Solicitacao s) {
        return new SolicitacaoResponse(
                s.getId(),
                new AlunoResponse(s.getAluno().getId(), s.getAluno().getNome(), s.getAluno().isAtivo()),
                new CursoResponse(s.getCurso().getId(), s.getCurso().getNome()),
                new TipoDocumentoResponse(s.getTipoDocumento().getId(), s.getTipoDocumento().getNome()),
                new StatusResponse(
                        s.getStatus().getId(),
                        s.getStatus().getCodigo(),
                        s.getStatus().getNome(),
                        s.getStatus().getResponsavel(),
                        s.getStatus().isFinalizaSolicitacao()),
                s.getPrioridade(),
                s.getDataSolicitacao(),
                s.getDataAlteracao(),
                s.getDataEmissao());
    }

    public SolicitacaoResumoResponse toResumo(Solicitacao s) {
        return new SolicitacaoResumoResponse(
                s.getId(),
                s.getAluno().getNome(),
                s.getCurso().getNome(),
                s.getTipoDocumento().getNome(),
                s.getStatus().getCodigo(),
                s.getPrioridade(),
                s.getDataSolicitacao());
    }
}