import { isAxiosError } from "axios";

/** Reaproveita a mensagem que o backend já escreve (ErroResponse.mensagem) — é específica e em português. */
export function extrairMensagemErro(erro: unknown, mensagemPadrao = "Não foi possível concluir a operação."): string {
  if (isAxiosError(erro) && typeof erro.response?.data?.mensagem === "string") {
    return erro.response.data.mensagem;
  }
  return mensagemPadrao;
}

/** Erros 400 de @Valid trazem ErroResponse.campos: [{campo, mensagem}] — mapeia para exibir junto de cada input. */
export function extrairErrosDeCampo(erro: unknown): Record<string, string> {
  if (isAxiosError(erro) && Array.isArray(erro.response?.data?.campos)) {
    const campos = erro.response.data.campos as { campo: string; mensagem: string }[];
    return Object.fromEntries(campos.map((c) => [c.campo, c.mensagem]));
  }
  return {};
}
