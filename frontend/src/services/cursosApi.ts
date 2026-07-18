import { api } from "./apiClient";
import type { PageResponse } from "./solicitacoesApi";

export interface Curso {
  id: number;
  nome: string;
}

export async function listarCursosPaginado(nome: string | undefined, page: number): Promise<PageResponse<Curso>> {
  const { data } = await api.get<PageResponse<Curso>>("/api/cursos", { params: { nome, page } });
  return data;
}

export async function criarCurso(nome: string): Promise<Curso> {
  const { data } = await api.post<Curso>("/api/cursos", { nome });
  return data;
}

export async function atualizarCurso(id: number, nome: string): Promise<Curso> {
  const { data } = await api.put<Curso>(`/api/cursos/${id}`, { nome });
  return data;
}

export async function excluirCurso(id: number): Promise<void> {
  await api.delete(`/api/cursos/${id}`);
}
