import { api } from "./apiClient";
import type { PageResponse } from "./solicitacoesApi";

export type Perfil = "ADMIN" | "OPERADOR" | "CONSULTA";

export interface Usuario {
  id: number;
  nome: string;
  login: string;
  codigoResponsavel: number | null;
  ativo: boolean;
  perfis: Perfil[];
}

export interface UsuarioFiltro {
  nome?: string;
  ativo?: boolean;
}

export interface UsuarioCreateRequest {
  nome: string;
  login: string;
  senha: string;
  codigoResponsavel: number | null;
  perfis: Perfil[];
}

export interface UsuarioAtualizacaoRequest {
  nome: string;
  codigoResponsavel: number | null;
  perfis: Perfil[];
}

export async function listarUsuariosPaginado(filtro: UsuarioFiltro, page: number): Promise<PageResponse<Usuario>> {
  const { data } = await api.get<PageResponse<Usuario>>("/api/usuarios", { params: { ...filtro, page } });
  return data;
}

export async function criarUsuario(request: UsuarioCreateRequest): Promise<Usuario> {
  const { data } = await api.post<Usuario>("/api/usuarios", request);
  return data;
}

export async function atualizarUsuario(id: number, request: UsuarioAtualizacaoRequest): Promise<Usuario> {
  const { data } = await api.put<Usuario>(`/api/usuarios/${id}`, request);
  return data;
}

export async function alterarSituacaoUsuario(id: number, ativo: boolean): Promise<Usuario> {
  const { data } = await api.patch<Usuario>(`/api/usuarios/${id}/ativo`, { ativo });
  return data;
}
