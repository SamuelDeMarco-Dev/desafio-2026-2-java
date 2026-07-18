package br.com.samuel.documentos_academicos.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Não auditada (Envers): o conteúdo binário duplicado nas tabelas _aud
 * dobraria o espaço em disco sem valor de auditoria — quem subiu e quando
 * já fica registrado nas próprias colunas.
 */
@Entity
@Table(name = "solicitacao_anexo")
@Getter @Setter
public class SolicitacaoAnexo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitacao_id", nullable = false)
    private Solicitacao solicitacao;

    @Column(name = "nome_arquivo", nullable = false, length = 255)
    private String nomeArquivo;

    @Column(name = "tipo_conteudo", nullable = false, length = 100)
    private String tipoConteudo;

    @Column(name = "tamanho_bytes", nullable = false)
    private long tamanhoBytes;

    /** byte[] sem @Lob mapeia para bytea no PostgreSQL (com @Lob viraria oid). */
    @Column(name = "dados", nullable = false)
    private byte[] dados;

    @Column(name = "data_upload", nullable = false)
    private LocalDateTime dataUpload;
}