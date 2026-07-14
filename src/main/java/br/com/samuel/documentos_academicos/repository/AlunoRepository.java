package br.com.samuel.documentos_academicos.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.samuel.documentos_academicos.entity.Aluno;

public interface AlunoRepository extends JpaRepository<Aluno, Long>{
    
    @Query("""
            select a from Aluno a
            where (:nome is null or lower(a.nome) like lower(concat('%', :nome, '%')))
            and (:ativo is null or a.ativo = :ativo)
            """)
    Page<Aluno> buscar(@Param("nome") String nome,
                       @Param("ativo") Boolean ativo,
                       Pageable pageable);
}
