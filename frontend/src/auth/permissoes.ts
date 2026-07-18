const PERFIS_QUE_GERENCIAM_SOLICITACOES = ["ADMIN", "OPERADOR"];
const PERFIS_QUE_GERENCIAM_CADASTROS = ["ADMIN"];

export function podeGerenciarSolicitacoes(perfis: string[]): boolean {
  return perfis.some((perfil) => PERFIS_QUE_GERENCIAM_SOLICITACOES.includes(perfil));
}

export function podeGerenciarCadastros(perfis: string[]): boolean {
  return perfis.some((perfil) => PERFIS_QUE_GERENCIAM_CADASTROS.includes(perfil));
}
