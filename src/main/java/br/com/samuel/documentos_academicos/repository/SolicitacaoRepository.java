package br.com.samuel.documentos_academicos.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.samuel.documentos_academicos.dto.response.ContagemStatusResponse;
import br.com.samuel.documentos_academicos.dto.response.ContagemTipoDocumentoResponse;
import br.com.samuel.documentos_academicos.dto.projection.IntervaloEmissao;
import br.com.samuel.documentos_academicos.entity.Solicitacao;

/*
 * As consultas de indicadores recebem sempre um intervalo fechado: quando o
 * período não é informado, o service passa limites amplos (ver DashboardServiceImpl).
 * Nenhum parâmetro nulo chega ao banco — o PostgreSQL não consegue inferir o tipo
 * de um parâmetro nulo e a consulta quebraria.
 */
public interface SolicitacaoRepository
        extends JpaRepository<Solicitacao, Long>, JpaSpecificationExecutor<Solicitacao> {

    boolean existsByAlunoId(Long alunoId);
    boolean existsByCursoId(Long cursoId);
    boolean existsByTipoDocumentoId(Long tipoDocumentoId);
    boolean existsByStatusId(Long statusId);

    /*
     * O resumo da listagem lê nome do aluno, do curso, do tipo e o código do
     * status — todos @ManyToOne(LAZY). Sem carregar o grafo junto, cada linha da
     * página dispara quatro selects extras (N+1). O ConsultasNMaisUmIT conta as
     * consultas e falha se alguém remover estas anotações.
     *
     * O fetch conjunto é seguro com paginação aqui porque são todas relações
     * *-to-one: não multiplicam linhas, então o banco continua paginando.
     */
    @Override
    @EntityGraph(attributePaths = {"aluno", "curso", "tipoDocumento", "status"})
    Page<Solicitacao> findAll(Specification<Solicitacao> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"aluno", "curso", "tipoDocumento", "status"})
    Page<Solicitacao> findByAlunoId(Long alunoId, Pageable pageable);

    @Query("""
           select new br.com.samuel.documentos_academicos.dto.response.ContagemStatusResponse(
                  s.status.codigo, count(s))
           from Solicitacao s
           where s.dataSolicitacao >= :inicio and s.dataSolicitacao < :fim
           group by s.status.codigo
           order by count(s) desc
           """)
    List<ContagemStatusResponse> contarPorStatus(@Param("inicio") LocalDateTime inicio,
                                                 @Param("fim") LocalDateTime fim);

    @Query("""
           select new br.com.samuel.documentos_academicos.dto.response.ContagemTipoDocumentoResponse(
                  s.tipoDocumento.nome, count(s))
           from Solicitacao s
           where s.dataSolicitacao >= :inicio and s.dataSolicitacao < :fim
           group by s.tipoDocumento.nome
           order by count(s) desc
           """)
    List<ContagemTipoDocumentoResponse> documentosMaisSolicitados(@Param("inicio") LocalDateTime inicio,
                                                                  @Param("fim") LocalDateTime fim);

    @Query("""
           select count(s) from Solicitacao s
           where s.dataSolicitacao >= :inicio and s.dataSolicitacao < :fim
           """)
    long contarNoPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("""
           select new br.com.samuel.documentos_academicos.dto.projection.IntervaloEmissao(
                  s.dataSolicitacao, s.dataEmissao)
           from Solicitacao s
           where s.dataEmissao is not null
             and s.dataSolicitacao >= :inicio and s.dataSolicitacao < :fim
           """)
    List<IntervaloEmissao> intervalosEmissao(@Param("inicio") LocalDateTime inicio,
                                             @Param("fim") LocalDateTime fim);
}