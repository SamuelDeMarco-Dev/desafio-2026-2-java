import { api } from "./apiClient";
import type { StatusResponse } from "./solicitacoesApi";

export interface StatusRequest {
  codigo: string;
  nome: string;
  responsavel: number | null;
  finalizaSolicitacao: boolean;
}

export async function listarStatusAdmin(): Promise<StatusResponse[]> {
  const { data } = await api.get<StatusResponse[]>("/api/status");
  return data;
}

export async function criarStatus(request: StatusRequest): Promise<StatusResponse> {
  const { data } = await api.post<StatusResponse>("/api/status", request);
  return data;
}

export async function atualizarStatus(id: number, request: StatusRequest): Promise<StatusResponse> {
  const { data } = await api.put<StatusResponse>(`/api/status/${id}`, request);
  return data;
}

export async function excluirStatus(id: number): Promise<void> {
  await api.delete(`/api/status/${id}`);
}
