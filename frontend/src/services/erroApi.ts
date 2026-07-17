import { isAxiosError } from "axios";

/** Reaproveita a mensagem que o backend já escreve (ErroResponse.mensagem) — é específica e em português. */
export function extrairMensagemErro(erro: unknown, mensagemPadrao = "Não foi possível concluir a operação."): string {
  if (isAxiosError(erro) && typeof erro.response?.data?.mensagem === "string") {
    return erro.response.data.mensagem;
  }
  return mensagemPadrao;
}
