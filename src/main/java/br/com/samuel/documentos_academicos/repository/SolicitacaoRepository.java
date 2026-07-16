package br.com.samuel.documentos_academicos.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.samuel.documentos_academicos.dto.response.ContagemStatusResponse;
import br.com.samuel.documentos_academicos.dto.response.ContagemTipoDocumentoResponse;
import br.com.samuel.documentos_academicos.dto.response.EmissaoIntervalo;
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
           select new br.com.samuel.documentos_academicos.dto.response.EmissaoIntervalo(
                  s.dataSolicitacao, s.dataEmissao)
           from Solicitacao s
           where s.dataEmissao is not null
             and s.dataSolicitacao >= :inicio and s.dataSolicitacao < :fim
           """)
    List<EmissaoIntervalo> intervalosEmissao(@Param("inicio") LocalDateTime inicio,
                                             @Param("fim") LocalDateTime fim);
}