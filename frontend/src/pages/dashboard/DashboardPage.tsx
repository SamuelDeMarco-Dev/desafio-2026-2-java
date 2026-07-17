import { useEffect, useState } from "react";
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { extrairMensagemErro } from "../../services/erroApi";
import {
  buscarDocumentosMaisSolicitados,
  buscarResumo,
  type ContagemTipoDocumento,
  type DashboardResumo,
  type PeriodoFiltro,
} from "../../services/dashboardApi";
import { FiltroPeriodo } from "./FiltroPeriodo";

export function DashboardPage() {
  const [periodo, setPeriodo] = useState<PeriodoFiltro>({});
  const [resumo, setResumo] = useState<DashboardResumo | null>(null);
  const [documentos, setDocumentos] = useState<ContagemTipoDocumento[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    setCarregando(true);
    setErro(null);
    Promise.all([buscarResumo(periodo), buscarDocumentosMaisSolicitados(periodo)])
      .then(([resumoResp, documentosResp]) => {
        setResumo(resumoResp);
        setDocumentos(documentosResp);
      })
      .catch((e) => setErro(extrairMensagemErro(e, "Não foi possível carregar os indicadores.")))
      .finally(() => setCarregando(false));
  }, [periodo]);

  return (
    <section>
      <div className="page-header">
        <h1>Indicadores</h1>
      </div>

      <FiltroPeriodo valorAplicado={periodo} aoAplicar={setPeriodo} />

      {erro && (
        <p role="alert" className="erro-banner">
          {erro}
        </p>
      )}
      {carregando && <p>Carregando...</p>}

      {!carregando && resumo && (
        <>
          <div className="cards-resumo">
            <div className="card">
              <span className="card-valor">{resumo.total}</span>
              <span className="card-rotulo">Solicitações no período</span>
            </div>
            <div className="card">
              <span className="card-valor">{resumo.tempoMedioEmissao.totalEmitidas}</span>
              <span className="card-rotulo">Documentos emitidos</span>
            </div>
            <div className="card">
              <span className="card-valor">
                {resumo.tempoMedioEmissao.totalEmitidas > 0
                  ? `${resumo.tempoMedioEmissao.diasMedios.toFixed(1)} dias`
                  : "—"}
              </span>
              <span className="card-rotulo">Tempo médio de emissão</span>
            </div>
          </div>

          <div className="dashboard-graficos">
            <div className="grafico-painel">
              <h2>Solicitações por status</h2>
              {resumo.porStatus.length === 0 ? (
                <p>Nenhuma solicitação no período.</p>
              ) : (
                <ResponsiveContainer width="100%" height={300}>
                  <BarChart data={resumo.porStatus}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="status" />
                    <YAxis allowDecimals={false} />
                    <Tooltip />
                    <Bar dataKey="total" fill="#2563eb" name="Solicitações" />
                  </BarChart>
                </ResponsiveContainer>
              )}
            </div>

            <div className="grafico-painel">
              <h2>Documentos mais solicitados</h2>
              {documentos.length === 0 ? (
                <p>Nenhum documento solicitado no período.</p>
              ) : (
                <ResponsiveContainer width="100%" height={300}>
                  <BarChart data={documentos} layout="vertical" margin={{ left: 24 }}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis type="number" allowDecimals={false} />
                    <YAxis type="category" dataKey="tipoDocumento" width={140} />
                    <Tooltip />
                    <Bar dataKey="total" fill="#16a34a" name="Solicitações" />
                  </BarChart>
                </ResponsiveContainer>
              )}
            </div>
          </div>
        </>
      )}
    </section>
  );
}
