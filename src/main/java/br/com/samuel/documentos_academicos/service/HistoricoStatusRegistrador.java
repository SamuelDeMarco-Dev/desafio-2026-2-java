package br.com.samuel.documentos_academicos.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import br.com.samuel.documentos_academicos.dto.response.HistoricoStatusResponse;
import br.com.samuel.documentos_academicos.entity.HistoricoStatus;
import br.com.samuel.documentos_academicos.entity.Solicitacao;
import br.com.samuel.documentos_academicos.entity.Status;
import br.com.samuel.documentos_academicos.entity.Usuario;
import br.com.samuel.documentos_academicos.mapper.HistoricoStatusMapper;
import br.com.samuel.documentos_academicos.repository.HistoricoStatusRepository;

/**
 * Registra e consulta o histórico de movimentações.
 *
 * <p>Extraído do SolicitacaoServiceImpl, que acumulava dez dependências ao
 * misturar o fluxo da solicitação com a escrita do histórico. Aqui o histórico
 * tem um dono só, e o service volta a tratar de solicitações.
 *
 * <p>Sem transação própria de propósito: os métodos participam da transação de
 * quem chama, para que uma movimentação recusada não deixe rastro.
 */
@Component
public class HistoricoStatusRegistrador {

    private final HistoricoStatusRepository historicoStatusRepository;
    private final HistoricoStatusMapper historicoStatusMapper;

    public HistoricoStatusRegistrador(HistoricoStatusRepository historicoStatusRepository,
                                      HistoricoStatusMapper historicoStatusMapper) {
        this.historicoStatusRepository = historicoStatusRepository;
        this.historicoStatusMapper = historicoStatusMapper;
    }

    /** Registra a abertura da solicitação (sem status anterior). */
    public void registrarAbertura(Solicitacao solicitacao, Status inicial, Usuario autor, LocalDateTime quando) {
        registrar(solicitacao, null, inicial, autor, quando);
    }

    /** Registra uma movimentação entre dois status. */
    public void registrarMovimentacao(Solicitacao solicitacao, Status anterior, Status novo,
                                      Usuario autor, LocalDateTime quando) {
        registrar(solicitacao, anterior, novo, autor, quando);
    }

    /** Histórico completo, em ordem cronológica. */
    public List<HistoricoStatusResponse> doSolicitacao(Long solicitacaoId) {
        return historicoStatusRepository
                .findBySolicitacaoIdOrderByDataMovimentacaoAscIdAsc(solicitacaoId)
                .stream().map(historicoStatusMapper::toResponse).toList();
    }

    private void registrar(Solicitacao solicitacao, Status anterior, Status novo,
                           Usuario autor, LocalDateTime quando) {
        HistoricoStatus h = new HistoricoStatus();
        h.setSolicitacao(solicitacao);
        h.setStatusAnterior(anterior);
        h.setStatusNovo(novo);
        h.setUsuario(autor);
        h.setDataMovimentacao(quando);
        historicoStatusRepository.save(h);
    }
}