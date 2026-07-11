-- V2: status iniciais do fluxo de solicitação

INSERT INTO status_solicitacao (
    codigo,
    nome,
    responsavel,
    finaliza_solicitacao
) VALUES
    ('ABERTA', 'Aberta', NULL, FALSE),
    ('EM_ANALISE', 'Em análise', NULL, FALSE),
    ('APROVADA', 'Aprovada', NULL, FALSE),
    ('EMITIDA', 'Emitida', NULL, TRUE),
    ('REPROVADA', 'Reprovada', NULL, TRUE);