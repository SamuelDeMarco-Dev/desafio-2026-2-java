package br.com.samuel.documentos_academicos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.samuel.documentos_academicos.dto.response.AnexoResponse;
import br.com.samuel.documentos_academicos.entity.SolicitacaoAnexo;

public interface SolicitacaoAnexoRepository extends JpaRepository<SolicitacaoAnexo, Long> {

    /**
     * Projeção sem a coluna 'dados': listar anexos não deve carregar os bytes
     * de todos os arquivos na memória — só o download individual faz isso.
     */
    @Query("""
           select new br.com.samuel.documentos_academicos.dto.response.AnexoResponse(
                  a.id, a.nomeArquivo, a.tipoConteudo, a.tamanhoBytes, a.dataUpload)
           from SolicitacaoAnexo a
           where a.solicitacao.id = :solicitacaoId
           order by a.dataUpload asc
           """)
    List<AnexoResponse> listarPorSolicitacao(@Param("solicitacaoId") Long solicitacaoId);

    Optional<SolicitacaoAnexo> findByIdAndSolicitacaoId(Long id, Long solicitacaoId);
}