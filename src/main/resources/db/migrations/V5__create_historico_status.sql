CREATE TABLE historico_status (
    id                BIGSERIAL PRIMARY KEY,
    solicitacao_id    BIGINT NOT NULL REFERENCES solicitacao(id),
    status_anterior_id BIGINT REFERENCES status_solicitacao(id),
    status_novo_id    BIGINT NOT NULL REFERENCES status_solicitacao(id),
    usuario_id        BIGINT NOT NULL REFERENCES usuario(id),
    data_movimentacao TIMESTAMP NOT NULL
);

CREATE INDEX idx_historico_solicitacao ON historico_status (solicitacao_id, data_movimentacao, id);
