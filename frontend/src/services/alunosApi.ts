import { api } from "./apiClient";
import type { PageResponse } from "./solicitacoesApi";

export interface Aluno {
  id: number;
  nome: string;
  ativo: boolean;
}

export interface AlunoFiltro {
  nome?: string;
  ativo?: boolean;
}

export async function listarAlunosPaginado(filtro: AlunoFiltro, page: number): Promise<PageResponse<Aluno>> {
  const { data } = await api.get<PageResponse<Aluno>>("/api/alunos", { params: { ...filtro, page } });
  return data;
}

export async function criarAluno(nome: string): Promise<Aluno> {
  const { data } = await api.post<Aluno>("/api/alunos", { nome });
  return data;
}

export async function atualizarAluno(id: number, nome: string): Promise<Aluno> {
  const { data } = await api.put<Aluno>(`/api/alunos/${id}`, { nome });
  return data;
}

export async function alterarSituacaoAluno(id: number, ativo: boolean): Promise<Aluno> {
  const { data } = await api.patch<Aluno>(`/api/alunos/${id}/ativo`, { ativo });
  return data;
}

export async function excluirAluno(id: number): Promise<void> {
  await api.delete(`/api/alunos/${id}`);
}
