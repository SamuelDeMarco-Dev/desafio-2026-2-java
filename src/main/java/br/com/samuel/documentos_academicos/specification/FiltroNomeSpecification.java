package br.com.samuel.documentos_academicos.specification;

import org.springframework.data.jpa.domain.Specification;

/**
 * Filtro parcial por nome, reaproveitável pelas entidades que têm o atributo
 * {@code nome} (Curso, TipoDocumento). Quando o filtro não é informado, devolve
 * um predicado sempre verdadeiro — assim nenhum parâmetro nulo vai ao banco.
 */
public final class FiltroNomeSpecification {

    private FiltroNomeSpecification() {
    }

    public static <T> Specification<T> contemNome(String nome) {
        return (root, query, cb) -> (nome == null || nome.isBlank())
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("nome")), "%" + nome.toLowerCase() + "%");
    }
}