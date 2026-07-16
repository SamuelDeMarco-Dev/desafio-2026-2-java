package br.com.samuel.documentos_academicos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.samuel.documentos_academicos.entity.HistoricoStatus;

public interface HistoricoStatusRepository extends JpaRepository<HistoricoStatus, Long> {

    // O desempate por id importa: movimentações no mesmo instante teriam ordem indefinida.
    @EntityGraph(attributePaths = {"statusAnterior", "statusNovo", "usuario"})
    List<HistoricoStatus> findBySolicitacaoIdOrderByDataMovimentacaoAscIdAsc(Long solicitacaoId);
}