import { useCallback, useEffect, useState, type FormEvent } from "react";
import { useParams } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { podeGerenciarSolicitacoes } from "../../auth/permissoes";
import { listarStatus } from "../../services/cadastrosApi";
import { extrairMensagemErro } from "../../services/erroApi";
import {
  alterarStatus,
  buscarHistorico,
  buscarSolicitacao,
  type HistoricoStatusItem,
  type SolicitacaoDetalhe,
  type StatusResponse,
} from "../../services/solicitacoesApi";

export function SolicitacaoDetalhePage() {
  const { id } = useParams<{ id: string }>();
  const solicitacaoId = Number(id);
  const { usuario } = useAuth();
  const gerencia = usuario ? podeGerenciarSolicitacoes(usuario.perfis) : false;

  const [solicitacao, setSolicitacao] = useState<SolicitacaoDetalhe | null>(null);
  const [statusDisponiveis, setStatusDisponiveis] = useState<StatusResponse[]>([]);
  const [historico, setHistorico] = useState<HistoricoStatusItem[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);

  const [statusDestinoId, setStatusDestinoId] = useState("");
  const [movimentando, setMovimentando] = useState(false);
  const [erroMovimentacao, setErroMovimentacao] = useState<string | null>(null);

  const carregar = useCallback(async () => {
    setCarregando(true);
    setErro(null);
    try {
      const [s, h] = await Promise.all([buscarSolicitacao(solicitacaoId), buscarHistorico(solicitacaoId)]);
      setSolicitacao(s);
      setHistorico(h);
    } catch (e) {
      setErro(extrairMensagemErro(e, "Não foi possível carregar a solicitação."));
    } finally {
      setCarregando(false);
    }
  }, [solicitacaoId]);

  useEffect(() => {
    carregar();
  }, [carregar]);

  useEffect(() => {
    listarStatus().then(setStatusDisponiveis).catch(() => setStatusDisponiveis([]));
  }, []);

  async function aoMovimentar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!usuario || !statusDestinoId) {
      return;
    }

    setMovimentando(true);
    setErroMovimentacao(null);
    try {
      await alterarStatus(solicitacaoId, {
        statusId: Number(statusDestinoId),
        codigoResponsavel: usuario.codigoResponsavel,
      });
      setStatusDestinoId("");
      await carregar(); // recarrega solicitação + histórico com o estado atual
    } catch (e) {
      setErroMovimentacao(extrairMensagemErro(e, "Não foi possível movimentar a solicitação."));
    } finally {
      setMovimentando(false);
    }
  }

  if (carregando) {
    return <p>Carregando...</p>;
  }
  if (erro) {
    return (
      <p role="alert" className="erro-banner">
        {erro}
      </p>
    );
  }
  if (!solicitacao) {
    return null;
  }

  const destinosPossiveis = statusDisponiveis.filter((s) => s.codigo !== solicitacao.status.codigo);

  return (
    <section>
      <h1>Solicitação #{solicitacao.id}</h1>

      <dl className="detalhe">
        <dt>Aluno</dt>
        <dd>{solicitacao.aluno.nome}</dd>
        <dt>Curso</dt>
        <dd>{solicitacao.curso.nome}</dd>
        <dt>Tipo de documento</dt>
        <dd>{solicitacao.tipoDocumento.nome}</dd>
        <dt>Status atual</dt>
        <dd>{solicitacao.status.nome}</dd>
        <dt>Prioridade</dt>
        <dd>{solicitacao.prioridade}</dd>
        <dt>Solicitada em</dt>
        <dd>{new Date(solicitacao.dataSolicitacao).toLocaleString("pt-BR")}</dd>
        <dt>Última alteração</dt>
        <dd>{new Date(solicitacao.dataAlteracao).toLocaleString("pt-BR")}</dd>
        {solicitacao.dataEmissao && (
          <>
            <dt>Emitida em</dt>
            <dd>{new Date(solicitacao.dataEmissao).toLocaleString("pt-BR")}</dd>
          </>
        )}
      </dl>

      {solicitacao.status.finalizaSolicitacao ? (
        <p>Esta solicitação está finalizada e não pode mais ser movimentada.</p>
      ) : gerencia ? (
        <form onSubmit={aoMovimentar} className="formulario-status">
          <label htmlFor="statusDestino">Mover para</label>
          <select id="statusDestino" value={statusDestinoId} onChange={(e) => setStatusDestinoId(e.target.value)} required>
            <option value="" disabled>
              Selecione o novo status...
            </option>
            {destinosPossiveis.map((s) => {
              const bloqueadoPorResponsavel = s.responsavel !== null && s.responsavel !== usuario?.codigoResponsavel;
              return (
                <option
                  key={s.id}
                  value={s.id}
                  disabled={bloqueadoPorResponsavel}
                  title={bloqueadoPorResponsavel ? "Apenas o responsável por este status pode movimentar para cá" : undefined}
                >
                  {s.nome}
                  {bloqueadoPorResponsavel ? " (restrito)" : ""}
                </option>
              );
            })}
          </select>

          {erroMovimentacao && (
            <p role="alert" className="erro-banner">
              {erroMovimentacao}
            </p>
          )}

          <button type="submit" disabled={movimentando || !statusDestinoId}>
            {movimentando ? "Movimentando..." : "Movimentar"}
          </button>
        </form>
      ) : (
        <p>Você não tem permissão para movimentar esta solicitação.</p>
      )}

      <h2>Histórico</h2>
      <table className="tabela">
        <thead>
          <tr>
            <th>De</th>
            <th>Para</th>
            <th>Responsável</th>
            <th>Quando</th>
          </tr>
        </thead>
        <tbody>
          {historico.map((h) => (
            <tr key={h.id}>
              <td>{h.statusAnterior?.nome ?? "—"}</td>
              <td>{h.statusNovo.nome}</td>
              <td>{h.responsavel.nome}</td>
              <td>{new Date(h.dataMovimentacao).toLocaleString("pt-BR")}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}
