-- V4: controle de concorrência otimista na solicitação
ALTER TABLE solicitacao ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
