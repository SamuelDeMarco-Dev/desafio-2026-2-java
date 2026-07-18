import { api } from "./apiClient";
import type { SolicitacaoFiltro } from "./solicitacoesApi";

/**
 * Baixa o relatório PDF com os mesmos filtros aplicados na listagem.
 * Download via axios (Authorization obrigatório) + link programático,
 * mesmo padrão do download de anexos.
 */
export async function baixarRelatorioSolicitacoes(filtro: SolicitacaoFiltro): Promise<void> {
  const { data } = await api.get<Blob>("/api/relatorios/solicitacoes", {
    params: filtro,
    responseType: "blob",
  });
  const url = URL.createObjectURL(data);
  const link = document.createElement("a");
  link.href = url;
  link.download = "relatorio-solicitacoes.pdf";
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
}