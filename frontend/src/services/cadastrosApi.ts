import { api } from "./apiClient";
import type { StatusResponse } from "./solicitacoesApi";

export interface Opcao {
  id: number;
  nome: string;
}

interface PageResponseGenerica<T> {
  content: T[];
}

/**
 * size=100: suficiente para o volume atual do projeto. Se crescer além disso,
 * o combo precisa virar busca com autocomplete no servidor — issue futura.
 */
const TAMANHO_PAGINA_OPCOES = 100;

export async function listarAlunosAtivos(): Promise<Opcao[]> {
  const { data } = await api.get<PageResponseGenerica<Opcao & { ativo: boolean }>>("/api/alunos", {
    params: { ativo: true, size: TAMANHO_PAGINA_OPCOES },
  });
  return data.content;
}

export async function listarCursos(): Promise<Opcao[]> {
  const { data } = await api.get<PageResponseGenerica<Opcao>>("/api/cursos", {
    params: { size: TAMANHO_PAGINA_OPCOES },
  });
  return data.content;
}

export async function listarTiposDocumento(): Promise<Opcao[]> {
  const { data } = await api.get<PageResponseGenerica<Opcao>>("/api/tipos-documento", {
    params: { size: TAMANHO_PAGINA_OPCOES },
  });
  return data.content;
}

export async function listarStatus(): Promise<StatusResponse[]> {
  const { data } = await api.get<StatusResponse[]>("/api/status");
  return data;
}
