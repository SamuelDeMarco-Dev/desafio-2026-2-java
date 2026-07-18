package br.com.samuel.documentos_academicos.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import br.com.samuel.documentos_academicos.dto.request.SolicitacaoFiltro;
import br.com.samuel.documentos_academicos.entity.Solicitacao;
import br.com.samuel.documentos_academicos.enums.Prioridade;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

public class SolicitacaoSpecification {

    private SolicitacaoSpecification() {
    }

    public static Specification<Solicitacao> comFiltros(SolicitacaoFiltro filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();

            if (temTexto(filtro.aluno())) {
                predicados.add(cb.like(cb.lower(root.get("aluno").get("nome")),
                        "%" + filtro.aluno().toLowerCase() + "%"));
            }
            if (temTexto(filtro.curso())) {
                predicados.add(cb.like(cb.lower(root.get("curso").get("nome")),
                        "%" + filtro.curso().toLowerCase() + "%"));
            }
            if (temTexto(filtro.tipoDocumento())) {
                predicados.add(cb.like(cb.lower(root.get("tipoDocumento").get("nome")),
                        "%" + filtro.tipoDocumento().toLowerCase() + "%"));
            }
            if (temTexto(filtro.status())) {
                predicados.add(cb.equal(cb.lower(root.get("status").get("codigo")),
                        filtro.status().toLowerCase()));
            } else {
                // Sem filtro de status, solicitações encerradas (EMITIDA/REPROVADA)
                // ficam fora da listagem — só aparecem quando filtradas explicitamente.
                predicados.add(cb.isFalse(root.get("status").get("finalizaSolicitacao")));
            }
            if (filtro.prioridade() != null) {
                predicados.add(cb.equal(root.get("prioridade"), filtro.prioridade()));
            }
            if (filtro.dataInicio() != null) {
                predicados.add(cb.greaterThanOrEqualTo(root.get("dataSolicitacao"),
                        filtro.dataInicio().atStartOfDay()));
            }
            if (filtro.dataFim() != null) {
                // fim inclusivo: tudo antes do início do dia seguinte
                predicados.add(cb.lessThan(root.get("dataSolicitacao"),
                        filtro.dataFim().plusDays(1).atStartOfDay()));
            }

            /*
             * Ordenação padrão: prioridade (URGENTE > ALTA > NORMAL) e, dentro da
             * mesma prioridade, a mais recente primeiro. A prioridade é gravada
             * como texto, então a ordem vem de um CASE — ordenar pela coluna
             * daria ordem alfabética (ALTA, NORMAL, URGENTE). A consulta de
             * count não aceita order by, daí o guard pelo tipo do resultado.
             * Se o cliente mandar ?sort=... explícito, o Pageable prevalece.
             */
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                Expression<Integer> ordemPrioridade = cb.<Integer>selectCase()
                        .when(cb.equal(root.get("prioridade"), Prioridade.URGENTE), 0)
                        .when(cb.equal(root.get("prioridade"), Prioridade.ALTA), 1)
                        .otherwise(2);
                query.orderBy(cb.asc(ordemPrioridade), cb.desc(root.get("dataSolicitacao")));
            }

            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }

    private static boolean temTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}