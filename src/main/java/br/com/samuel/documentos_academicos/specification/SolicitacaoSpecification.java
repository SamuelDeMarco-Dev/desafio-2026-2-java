package br.com.samuel.documentos_academicos.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import br.com.samuel.documentos_academicos.dto.request.SolicitacaoFiltro;
import br.com.samuel.documentos_academicos.entity.Solicitacao;
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

            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }

    private static boolean temTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
