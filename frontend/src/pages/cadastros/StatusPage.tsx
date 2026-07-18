import { useCallback, useEffect, useState, type FormEvent } from "react";
import { useAuth } from "../../auth/AuthContext";
import { podeGerenciarCadastros } from "../../auth/permissoes";
import { extrairErrosDeCampo, extrairMensagemErro } from "../../services/erroApi";
import type { StatusResponse } from "../../services/solicitacoesApi";
import {
  atualizarStatus,
  criarStatus,
  excluirStatus,
  listarStatusAdmin,
  type StatusRequest,
} from "../../services/statusApi";

/**
 * Só para desabilitar campos na UI. A regra de verdade é do backend
 * (StatusServiceImpl rejeita mudança de código/finalização com 422).
 */
const CODIGOS_ESTRUTURAIS = ["ABERTA", "EM_ANALISE", "APROVADA", "EMITIDA", "REPROVADA"];

interface StatusForm {
  codigo: string;
  nome: string;
  responsavel: string;
  finalizaSolicitacao: boolean;
}

const FORM_VAZIO: StatusForm = { codigo: "", nome: "", responsavel: "", finalizaSolicitacao: false };

function paraRequest(form: StatusForm): StatusRequest {
  return {
    codigo: form.codigo.trim(),
    nome: form.nome.trim(),
    responsavel: form.responsavel.trim() ? Number(form.responsavel) : null,
    finalizaSolicitacao: form.finalizaSolicitacao,
  };
}

export function StatusPage() {
  const { usuario } = useAuth();
  const gerencia = usuario ? podeGerenciarCadastros(usuario.perfis) : false;

  const [lista, setLista] = useState<StatusResponse[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);

  const [mostrarForm, setMostrarForm] = useState(false);
  const [editando, setEditando] = useState<StatusResponse | null>(null);
  const [form, setForm] = useState<StatusForm>(FORM_VAZIO);
  const [errosCampo, setErrosCampo] = useState<Record<string, string>>({});
  const [salvando, setSalvando] = useState(false);

  const carregar = useCallback(() => {
    setCarregando(true);
    setErro(null);
    listarStatusAdmin()
      .then(setLista)
      .catch((e) => setErro(extrairMensagemErro(e, "Não foi possível carregar os status.")))
      .finally(() => setCarregando(false));
  }, []);

  useEffect(() => {
    carregar();
  }, [carregar]);

  function aoNovo() {
    setEditando(null);
    setForm(FORM_VAZIO);
    setErrosCampo({});
    setMostrarForm(true);
  }

  function aoEditar(status: StatusResponse) {
    setEditando(status);
    setForm({
      codigo: status.codigo,
      nome: status.nome,
      responsavel: status.responsavel !== null ? String(status.responsavel) : "",
      finalizaSolicitacao: status.finalizaSolicitacao,
    });
    setErrosCampo({});
    setMostrarForm(true);
  }

  const campoEstruturalTravado = editando !== null && CODIGOS_ESTRUTURAIS.includes(editando.codigo);

  async function aoSalvar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSalvando(true);
    setErro(null);
    setErrosCampo({});
    try {
      const request = paraRequest(form);
      if (editando !== null) {
        await atualizarStatus(editando.id, request);
      } else {
        await criarStatus(request);
      }
      setMostrarForm(false);
      carregar();
    } catch (e) {
      setErro(extrairMensagemErro(e, "Não foi possível salvar o status."));
      setErrosCampo(extrairErrosDeCampo(e));
    } finally {
      setSalvando(false);
    }
  }

  async function aoExcluir(status: StatusResponse) {
    if (!window.confirm(`Excluir o status "${status.nome}"?`)) {
      return;
    }
    setErro(null);
    try {
      await excluirStatus(status.id);
      carregar();
    } catch (e) {
      setErro(extrairMensagemErro(e, "Não foi possível excluir o status."));
    }
  }

  return (
    <section>
      <div className="page-header">
        <h1>Status</h1>
        {gerencia && !mostrarForm && (
          <button type="button" className="botao-primario" onClick={aoNovo}>
            Novo status
          </button>
        )}
      </div>

      {erro && (
        <p role="alert" className="erro-banner">
          {erro}
        </p>
      )}

      {gerencia && mostrarForm && (
        <form onSubmit={aoSalvar} className="formulario">
          <label htmlFor="codigo">Código</label>
          <input
            id="codigo"
            value={form.codigo}
            onChange={(e) => setForm({ ...form, codigo: e.target.value.toUpperCase() })}
            required
            maxLength={30}
            disabled={campoEstruturalTravado}
            title={campoEstruturalTravado ? "Status estrutural: código imutável." : undefined}
          />
          {errosCampo.codigo && <p className="erro-campo">{errosCampo.codigo}</p>}

          <label htmlFor="nome-status">Nome</label>
          <input
            id="nome-status"
            value={form.nome}
            onChange={(e) => setForm({ ...form, nome: e.target.value })}
            required
            maxLength={100}
          />
          {errosCampo.nome && <p className="erro-campo">{errosCampo.nome}</p>}

          <label htmlFor="responsavel">Código do responsável (opcional)</label>
          <input
            id="responsavel"
            type="number"
            value={form.responsavel}
            onChange={(e) => setForm({ ...form, responsavel: e.target.value })}
          />

          <label>
            <input
              type="checkbox"
              checked={form.finalizaSolicitacao}
              onChange={(e) => setForm({ ...form, finalizaSolicitacao: e.target.checked })}
              disabled={campoEstruturalTravado}
              title={campoEstruturalTravado ? "Status estrutural: finalização imutável." : undefined}
            />{" "}
            Finaliza a solicitação
          </label>

          <div className="formulario-acoes">
            <button type="submit" disabled={salvando}>
              {salvando ? "Salvando..." : "Salvar"}
            </button>
            <button type="button" onClick={() => setMostrarForm(false)}>
              Cancelar
            </button>
          </div>
        </form>
      )}

      {carregando && <p>Carregando...</p>}

      {!carregando && (
        <table className="tabela">
          <thead>
            <tr>
              <th>Código</th>
              <th>Nome</th>
              <th>Responsável</th>
              <th>Finaliza?</th>
              {gerencia && <th>Ações</th>}
            </tr>
          </thead>
          <tbody>
            {lista.map((status) => (
              <tr key={status.id}>
                <td>{status.codigo}</td>
                <td>{status.nome}</td>
                <td>{status.responsavel ?? "—"}</td>
                <td>{status.finalizaSolicitacao ? "Sim" : "Não"}</td>
                {gerencia && (
                  <td className="acoes">
                    <button type="button" onClick={() => aoEditar(status)}>
                      Editar
                    </button>
                    {!CODIGOS_ESTRUTURAIS.includes(status.codigo) && (
                      <button type="button" onClick={() => aoExcluir(status)}>
                        Excluir
                      </button>
                    )}
                  </td>
                )}
              </tr>
            ))}
            {lista.length === 0 && (
              <tr>
                <td colSpan={gerencia ? 5 : 4}>Nenhum status cadastrado.</td>
              </tr>
            )}
          </tbody>
        </table>
      )}
    </section>
  );
}
