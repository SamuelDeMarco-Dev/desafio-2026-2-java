import { useEffect, useState, type FormEvent } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { podeGerenciarSolicitacoes } from "../../auth/permissoes";
import { listarStatus } from "../../services/cadastrosApi";
import { extrairMensagemErro } from "../../services/erroApi";
import {
  listarSolicitacoes,
  type PageResponse,
  type Prioridade,
  type SolicitacaoFiltro,
  type SolicitacaoResumo,
  type StatusResponse,
} from "../../services/solicitacoesApi";

const PRIORIDADES: Prioridade[] = ["URGENTE", "ALTA", "NORMAL"];

interface FiltrosForm {
  aluno: string;
  curso: string;
  tipoDocumento: string;
  status: string;
  prioridade: string;
  dataInicio: string;
  dataFim: string;
}

const FILTROS_VAZIOS: FiltrosForm = {
  aluno: "",
  curso: "",
  tipoDocumento: "",
  status: "",
  prioridade: "",
  dataInicio: "",
  dataFim: "",
};

function paraFiltroApi(form: FiltrosForm): SolicitacaoFiltro {
  return {
    aluno: form.aluno.trim() || undefined,
    curso: form.curso.trim() || undefined,
    tipoDocumento: form.tipoDocumento.trim() || undefined,
    status: form.status || undefined,
    prioridade: (form.prioridade as Prioridade) || undefined,
    dataInicio: form.dataInicio || undefined,
    dataFim: form.dataFim || undefined,
  };
}

export function SolicitacoesListPage() {
  const { usuario } = useAuth();
  const gerencia = usuario ? podeGerenciarSolicitacoes(usuario.perfis) : false;

  const [statusDisponiveis, setStatusDisponiveis] = useState<StatusResponse[]>([]);
  const [filtrosForm, setFiltrosForm] = useState<FiltrosForm>(FILTROS_VAZIOS);
  const [filtrosAplicados, setFiltrosAplicados] = useState<SolicitacaoFiltro>({});
  const [pagina, setPagina] = useState(0);
  const [resultado, setResultado] = useState<PageResponse<SolicitacaoResumo> | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    listarStatus().then(setStatusDisponiveis).catch(() => setStatusDisponiveis([]));
  }, []);

  useEffect(() => {
    setCarregando(true);
    setErro(null);
    listarSolicitacoes(filtrosAplicados, pagina)
      .then(setResultado)
      .catch((e) => setErro(extrairMensagemErro(e, "Não foi possível carregar as solicitações.")))
      .finally(() => setCarregando(false));
  }, [filtrosAplicados, pagina]);

  function aoFiltrar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setPagina(0);
    setFiltrosAplicados(paraFiltroApi(filtrosForm));
  }

  function aoLimparFiltros() {
    setFiltrosForm(FILTROS_VAZIOS);
    setPagina(0);
    setFiltrosAplicados({});
  }

  return (
    <section>
      <div className="page-header">
        <h1>Solicitações</h1>
        {gerencia && (
          <Link to="/solicitacoes/nova" className="botao-primario">
            Nova solicitação
          </Link>
        )}
      </div>

      <form onSubmit={aoFiltrar} className="filtros">
        <input
          placeholder="Aluno"
          value={filtrosForm.aluno}
          onChange={(e) => setFiltrosForm({ ...filtrosForm, aluno: e.target.value })}
        />
        <input
          placeholder="Curso"
          value={filtrosForm.curso}
          onChange={(e) => setFiltrosForm({ ...filtrosForm, curso: e.target.value })}
        />
        <input
          placeholder="Tipo de documento"
          value={filtrosForm.tipoDocumento}
          onChange={(e) => setFiltrosForm({ ...filtrosForm, tipoDocumento: e.target.value })}
        />
        <select value={filtrosForm.status} onChange={(e) => setFiltrosForm({ ...filtrosForm, status: e.target.value })}>
          <option value="">Todos os status</option>
          {statusDisponiveis.map((s) => (
            <option key={s.id} value={s.codigo}>
              {s.nome}
            </option>
          ))}
        </select>
        <select
          value={filtrosForm.prioridade}
          onChange={(e) => setFiltrosForm({ ...filtrosForm, prioridade: e.target.value })}
        >
          <option value="">Todas as prioridades</option>
          {PRIORIDADES.map((p) => (
            <option key={p} value={p}>
              {p}
            </option>
          ))}
        </select>
        <input
          type="date"
          value={filtrosForm.dataInicio}
          onChange={(e) => setFiltrosForm({ ...filtrosForm, dataInicio: e.target.value })}
        />
        <input
          type="date"
          value={filtrosForm.dataFim}
          onChange={(e) => setFiltrosForm({ ...filtrosForm, dataFim: e.target.value })}
        />
        <button type="submit">Filtrar</button>
        <button type="button" onClick={aoLimparFiltros}>
          Limpar
        </button>
      </form>

      {erro && (
        <p role="alert" className="erro-banner">
          {erro}
        </p>
      )}
      {carregando && <p>Carregando...</p>}

      {!carregando && resultado && (
        <>
          <table className="tabela">
            <thead>
              <tr>
                <th>Aluno</th>
                <th>Curso</th>
                <th>Tipo de documento</th>
                <th>Status</th>
                <th>Prioridade</th>
                <th>Solicitada em</th>
              </tr>
            </thead>
            <tbody>
              {resultado.content.map((s) => (
                <tr key={s.id}>
                  <td>
                    <Link to={`/solicitacoes/${s.id}`}>{s.alunoNome}</Link>
                  </td>
                  <td>{s.cursoNome}</td>
                  <td>{s.tipoDocumentoNome}</td>
                  <td>{s.statusCodigo}</td>
                  <td>{s.prioridade}</td>
                  <td>{new Date(s.dataSolicitacao).toLocaleString("pt-BR")}</td>
                </tr>
              ))}
              {resultado.content.length === 0 && (
                <tr>
                  <td colSpan={6}>Nenhuma solicitação encontrada.</td>
                </tr>
              )}
            </tbody>
          </table>

          <div className="paginacao">
            <button disabled={resultado.first} onClick={() => setPagina((p) => p - 1)}>
              Anterior
            </button>
            <span>
              Página {resultado.page + 1} de {Math.max(resultado.totalPages, 1)}
            </span>
            <button disabled={resultado.last} onClick={() => setPagina((p) => p + 1)}>
              Próxima
            </button>
          </div>
        </>
      )}
    </section>
  );
}
