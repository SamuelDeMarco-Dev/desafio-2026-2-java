-- V8: autorrecuperação de senha ("esqueci minha senha").
-- Guarda apenas o hash do código de recuperação e sua validade.
ALTER TABLE usuario ADD COLUMN recuperacao_token_hash VARCHAR(100);
ALTER TABLE usuario ADD COLUMN recuperacao_expira_em TIMESTAMP;