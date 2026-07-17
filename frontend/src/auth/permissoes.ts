const PERFIS_QUE_GERENCIAM_SOLICITACOES = ["ADMIN", "OPERADOR"];

export function podeGerenciarSolicitacoes(perfis: string[]): boolean {
  return perfis.some((perfil) => PERFIS_QUE_GERENCIAM_SOLICITACOES.includes(perfil));
}
