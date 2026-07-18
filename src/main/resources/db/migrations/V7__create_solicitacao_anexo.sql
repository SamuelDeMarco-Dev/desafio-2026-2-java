-- Anexos (documentos) vinculados às solicitações.
-- Conteúdo armazenado no próprio banco (bytea): volume esperado é baixo e
-- simplifica backup/deploy — sem filesystem compartilhado entre containers.
CREATE TABLE solicitacao_anexo (
    id              BIGSERIAL PRIMARY KEY,
    solicitacao_id  BIGINT       NOT NULL REFERENCES solicitacao (id),
    nome_arquivo    VARCHAR(255) NOT NULL,
    tipo_conteudo   VARCHAR(100) NOT NULL,
    tamanho_bytes   BIGINT       NOT NULL,
    dados           BYTEA        NOT NULL,
    data_upload     TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_solicitacao_anexo_solicitacao ON solicitacao_anexo (solicitacao_id);