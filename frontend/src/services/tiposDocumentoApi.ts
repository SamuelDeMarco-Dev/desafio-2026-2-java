import { api } from "./apiClient";
import type { PageResponse } from "./solicitacoesApi";

export interface TipoDocumento {
  id: number;
  nome: string;
}

export async function listarTiposDocumentoPaginado(
  nome: string | undefined,
  page: number
): Promise<PageResponse<TipoDocumento>> {
  const { data } = await api.get<PageResponse<TipoDocumento>>("/api/tipos-documento", { params: { nome, page } });
  return data;
}

export async function criarTipoDocumento(nome: string): Promise<TipoDocumento> {
  const { data } = await api.post<TipoDocumento>("/api/tipos-documento", { nome });
  return data;
}

export async function atualizarTipoDocumento(id: number, nome: string): Promise<TipoDocumento> {
  const { data } = await api.put<TipoDocumento>(`/api/tipos-documento/${id}`, { nome });
  return data;
}

export async function excluirTipoDocumento(id: number): Promise<void> {
  await api.delete(`/api/tipos-documento/${id}`);
}
