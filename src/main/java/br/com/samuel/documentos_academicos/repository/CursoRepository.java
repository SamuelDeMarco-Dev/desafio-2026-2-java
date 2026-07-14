package br.com.samuel.documentos_academicos.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.samuel.documentos_academicos.entity.Curso;

public interface CursoRepository extends JpaRepository<Curso, Long> {
    @Query("""
            select c from Curso c
            where (:nome is null or lower(c.nome) like lower(concat('%', :nome, '%')))
            """)
    Page<Curso> buscar(@Param("nome") String nome, Pageable pageable);

    boolean existsByNomeIgnoreCase(String nome);
    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
}
