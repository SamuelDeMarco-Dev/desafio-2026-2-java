-- Auditoria (Hibernate Envers): uma linha em revinfo por transação auditada,
-- e uma linha em <tabela>_aud por entidade tocada nessa transação.

CREATE SEQUENCE revinfo_seq INCREMENT BY 1 START WITH 1;

CREATE TABLE revinfo (
    rev           BIGINT PRIMARY KEY,
    revtstmp      BIGINT,
    usuario_login VARCHAR(50)
);

-- revtype: 0 = inclusão, 1 = alteração, 2 = exclusão

CREATE TABLE aluno_aud (
    id      BIGINT   NOT NULL,
    rev     BIGINT   NOT NULL REFERENCES revinfo(rev),
    revtype SMALLINT,
    nome    VARCHAR(150),
    ativo   BOOLEAN,
    PRIMARY KEY (id, rev)
);

CREATE TABLE curso_aud (
    id      BIGINT   NOT NULL,
    rev     BIGINT   NOT NULL REFERENCES revinfo(rev),
    revtype SMALLINT,
    nome    VARCHAR(150),
    PRIMARY KEY (id, rev)
);

CREATE TABLE tipo_documento_aud (
    id      BIGINT   NOT NULL,
    rev     BIGINT   NOT NULL REFERENCES revinfo(rev),
    revtype SMALLINT,
    nome    VARCHAR(150),
    PRIMARY KEY (id, rev)
);

CREATE TABLE status_solicitacao_aud (
    id                  BIGINT   NOT NULL,
    rev                 BIGINT   NOT NULL REFERENCES revinfo(rev),
    revtype             SMALLINT,
    codigo              VARCHAR(30),
    nome                VARCHAR(100),
    responsavel         INTEGER,
    finaliza_solicitacao BOOLEAN,
    PRIMARY KEY (id, rev)
);

CREATE TABLE solicitacao_aud (
    id                BIGINT   NOT NULL,
    rev               BIGINT   NOT NULL REFERENCES revinfo(rev),
    revtype           SMALLINT,
    aluno_id          BIGINT,
    curso_id          BIGINT,
    tipo_documento_id BIGINT,
    status_id         BIGINT,
    data_solicitacao  TIMESTAMP,
    data_alteracao    TIMESTAMP,
    data_emissao      TIMESTAMP,
    prioridade        VARCHAR(20),
    PRIMARY KEY (id, rev)
);

CREATE TABLE usuario_aud (
    id                 BIGINT   NOT NULL,
    rev                BIGINT   NOT NULL REFERENCES revinfo(rev),
    revtype            SMALLINT,
    nome               VARCHAR(150),
    login              VARCHAR(50),
    codigo_responsavel INTEGER,
    ativo              BOOLEAN,
    PRIMARY KEY (id, rev)
);

CREATE TABLE usuario_perfil_aud (
    rev        BIGINT      NOT NULL REFERENCES revinfo(rev),
    revtype    SMALLINT,
    usuario_id BIGINT      NOT NULL,
    perfil     VARCHAR(20) NOT NULL,
    PRIMARY KEY (rev, usuario_id, perfil)
);

CREATE INDEX idx_aluno_aud_rev            ON aluno_aud (rev);
CREATE INDEX idx_curso_aud_rev            ON curso_aud (rev);
CREATE INDEX idx_tipo_documento_aud_rev   ON tipo_documento_aud (rev);
CREATE INDEX idx_status_aud_rev           ON status_solicitacao_aud (rev);
CREATE INDEX idx_solicitacao_aud_rev      ON solicitacao_aud (rev);
CREATE INDEX idx_usuario_aud_rev          ON usuario_aud (rev);
