package br.com.samuel.documentos_academicos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.samuel.documentos_academicos.entity.Solicitacao;

public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long>{
    boolean existsByAlunoId(Long alunoId);
    boolean existsByCursoId(Long cursoId);
    boolean existsByTipoDocumentoId(Long tipoDocumentoId);
}
