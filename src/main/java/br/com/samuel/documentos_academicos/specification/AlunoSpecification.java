package br.com.samuel.documentos_academicos.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import br.com.samuel.documentos_academicos.entity.Aluno;
import jakarta.persistence.criteria.Predicate;

public final class AlunoSpecification {

    private AlunoSpecification() {
    }

    /**
     * Filtros opcionais de aluno. Cada predicado só entra na consulta se o filtro
     * for informado — nenhum parâmetro nulo chega ao banco.
     */
    public static Specification<Aluno> comFiltros(String nome, Boolean ativo) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();

            if (nome != null && !nome.isBlank()) {
                predicados.add(cb.like(cb.lower(root.get("nome")), "%" + nome.toLowerCase() + "%"));
            }
            if (ativo != null) {
                predicados.add(cb.equal(root.get("ativo"), ativo));
            }

            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }
}