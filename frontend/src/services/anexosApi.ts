import { api } from "./apiClient";

export interface Anexo {
  id: number;
  nomeArquivo: string;
  tipoConteudo: string;
  tamanhoBytes: number;
  dataUpload: string;
}

export async function listarAnexos(solicitacaoId: number): Promise<Anexo[]> {
  const { data } = await api.get<Anexo[]>(`/api/solicitacoes/${solicitacaoId}/anexos`);
  return data;
}

export async function enviarAnexo(solicitacaoId: number, arquivo: File): Promise<Anexo> {
  const formData = new FormData();
  formData.append("arquivo", arquivo);
  // Content-Type multipart: o axios troca o application/json padrão e o
  // navegador acrescenta o boundary automaticamente.
  const { data } = await api.post<Anexo>(`/api/solicitacoes/${solicitacaoId}/anexos`, formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
  return data;
}

/**
 * Download via axios (o header Authorization é obrigatório, então um <a href>
 * direto não funciona): baixa o blob e dispara o download programaticamente.
 */
export async function baixarAnexo(solicitacaoId: number, anexo: Anexo): Promise<void> {
  const { data } = await api.get<Blob>(`/api/solicitacoes/${solicitacaoId}/anexos/${anexo.id}`, {
    responseType: "blob",
  });
  const url = URL.createObjectURL(data);
  const link = document.createElement("a");
  link.href = url;
  link.download = anexo.nomeArquivo;
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
}

export async function excluirAnexo(solicitacaoId: number, anexoId: number): Promise<void> {
  await api.delete(`/api/solicitacoes/${solicitacaoId}/anexos/${anexoId}`);
}

export function formatarTamanho(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}