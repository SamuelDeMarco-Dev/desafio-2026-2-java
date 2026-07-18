import { api } from "./apiClient";

export interface ContagemStatus {
  status: string;
  total: number;
}

export interface ContagemTipoDocumento {
  tipoDocumento: string;
  total: number;
}

export interface TempoMedioEmissao {
  diasMedios: number;
  totalEmitidas: number;
}

export interface DashboardResumo {
  total: number;
  porStatus: ContagemStatus[];
  tempoMedioEmissao: TempoMedioEmissao;
}

export interface PeriodoFiltro {
  dataInicio?: string;
  dataFim?: string;
}

function paramsDe(periodo: PeriodoFiltro): Record<string, string> {
  const params: Record<string, string> = {};
  if (periodo.dataInicio) params.dataInicio = periodo.dataInicio;
  if (periodo.dataFim) params.dataFim = periodo.dataFim;
  return params;
}

export async function buscarResumo(periodo: PeriodoFiltro): Promise<DashboardResumo> {
  const { data } = await api.get<DashboardResumo>("/api/dashboard/resumo", { params: paramsDe(periodo) });
  return data;
}

export async function buscarSolicitacoesPorStatus(periodo: PeriodoFiltro): Promise<ContagemStatus[]> {
  const { data } = await api.get<ContagemStatus[]>("/api/dashboard/solicitacoes-por-status", {
    params: paramsDe(periodo),
  });
  return data;
}

export async function buscarDocumentosMaisSolicitados(periodo: PeriodoFiltro): Promise<ContagemTipoDocumento[]> {
  const { data } = await api.get<ContagemTipoDocumento[]>("/api/dashboard/documentos-mais-solicitados", {
    params: paramsDe(periodo),
  });
  return data;
}

export async function buscarTempoMedioEmissao(periodo: PeriodoFiltro): Promise<TempoMedioEmissao> {
  const { data } = await api.get<TempoMedioEmissao>("/api/dashboard/tempo-medio-emissao", {
    params: paramsDe(periodo),
  });
  return data;
}
