import { api } from "./apiClient";

export type Prioridade = "URGENTE" | "ALTA" | "NORMAL";

export interface StatusResponse {
  id: number;
  codigo: string;
  nome: string;
  responsavel: number | null;
  finalizaSolicitacao: boolean;
}

export interface SolicitacaoResumo {
  id: number;
  alunoNome: string;
  cursoNome: string;
  tipoDocumentoNome: string;
  statusCodigo: string;
  prioridade: Prioridade;
  dataSolicitacao: string;
}

export interface SolicitacaoDetalhe {
  id: number;
  aluno: { id: number; nome: string; ativo: boolean };
  curso: { id: number; nome: string };
  tipoDocumento: { id: number; nome: string };
  status: StatusResponse;
  prioridade: Prioridade;
  dataSolicitacao: string;
  dataAlteracao: string;
  dataEmissao: string | null;
}

export interface HistoricoStatusItem {
  id: number;
  statusAnterior: StatusResponse | null;
  statusNovo: StatusResponse;
  responsavel: { id: number; nome: string; codigoResponsavel: number };
  dataMovimentacao: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface SolicitacaoFiltro {
  aluno?: string;
  curso?: string;
  tipoDocumento?: string;
  status?: string;
  prioridade?: Prioridade;
  dataInicio?: string;
  dataFim?: string;
}

export interface SolicitacaoCreateRequest {
  alunoId: number;
  cursoId: number;
  tipoDocumentoId: number;
  prioridade?: Prioridade;
}

export interface AlteracaoStatusRequest {
  statusId: number;
  codigoResponsavel: number;
}

export async function listarSolicitacoes(
  filtro: SolicitacaoFiltro,
  page: number
): Promise<PageResponse<SolicitacaoResumo>> {
  const { data } = await api.get<PageResponse<SolicitacaoResumo>>("/api/solicitacoes", {
    params: { ...filtro, page },
  });
  return data;
}

export async function buscarSolicitacao(id: number): Promise<SolicitacaoDetalhe> {
  const { data } = await api.get<SolicitacaoDetalhe>(`/api/solicitacoes/${id}`);
  return data;
}

export async function criarSolicitacao(request: SolicitacaoCreateRequest): Promise<SolicitacaoDetalhe> {
  const { data } = await api.post<SolicitacaoDetalhe>("/api/solicitacoes", request);
  return data;
}

export async function alterarStatus(id: number, request: AlteracaoStatusRequest): Promise<SolicitacaoDetalhe> {
  const { data } = await api.patch<SolicitacaoDetalhe>(`/api/solicitacoes/${id}/status`, request);
  return data;
}

export async function buscarHistorico(id: number): Promise<HistoricoStatusItem[]> {
  const { data } = await api.get<HistoricoStatusItem[]>(`/api/solicitacoes/${id}/historico`);
  return data;
}
