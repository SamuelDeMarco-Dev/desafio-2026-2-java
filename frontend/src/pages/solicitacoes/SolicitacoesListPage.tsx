import { useEffect, useState, type FormEvent } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { podeGerenciarSolicitacoes } from "../../auth/permissoes";
import { ChipSelect } from "../../components/ChipSelect";
import { FluxoBpmnModal } from "../../components/FluxoBpmnModal";
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
  const [statusFluxo, setStatusFluxo] = useState<string | null>(null);

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

  const opcoesStatus = [{ value: "", label: "Todos" }, ...statusDisponiveis.map((s) => ({ value: s.codigo, label: s.nome }))];
  const opcoesPrioridade = [{ value: "", label: "Todas" }, ...PRIORIDADES.map((p) => ({ value: p, label: p }))];

  return (
    <section>
      <div className="page-header">
        <h1>Solicitações</h1>
        {gerencia && (
          <Link to="/solicitacoes/nova" className="botao-primario">
            + Nova solicitação
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

      <div className="grupo-filtro">
        <span className="grupo-filtro-rotulo">Status</span>
        <ChipSelect
          options={opcoesStatus}
          value={filtrosForm.status}
          onChange={(v) => setFiltrosForm({ ...filtrosForm, status: v })}
          nomeGrupo="Status"
        />
      </div>
      <div className="grupo-filtro">
        <span className="grupo-filtro-rotulo">Prioridade</span>
        <ChipSelect
          options={opcoesPrioridade}
          value={filtrosForm.prioridade}
          onChange={(v) => setFiltrosForm({ ...filtrosForm, prioridade: v })}
          nomeGrupo="Prioridade"
        />
      </div>

      {erro && (
        <p role="alert" className="erro-banner">
          {erro}
        </p>
      )}
      {carregando && <p>Carregando...</p>}

      {!carregando && resultado && (
        <>
          <table className="tabela tabela-cards">
            <thead>
              <tr>
                <th>Aluno</th>
                <th>Curso</th>
                <th>Tipo de documento</th>
                <th>Status</th>
                <th>Prioridade</th>
                <th>Solicitada em</th>
                <th>Fluxo</th>
              </tr>
            </thead>
            <tbody>
              {resultado.content.map((s) => (
                <tr key={s.id}>
                  <td data-label="Aluno">
                    <Link to={`/solicitacoes/${s.id}`}>{s.alunoNome}</Link>
                  </td>
                  <td data-label="Curso">{s.cursoNome}</td>
                  <td data-label="Tipo de documento">{s.tipoDocumentoNome}</td>
                  <td data-label="Status">
                    <span className={`badge badge-status-${s.statusCodigo}`}>{s.statusCodigo.replace("_", " ")}</span>
                  </td>
                  <td data-label="Prioridade">
                    <span className={`badge badge-prioridade-${s.prioridade}`}>{s.prioridade}</span>
                  </td>
                  <td data-label="Solicitada em">{new Date(s.dataSolicitacao).toLocaleString("pt-BR")}</td>
                  <td data-label="Fluxo">
                    <button
                      type="button"
                      className="icone-botao"
                      aria-label="Ver fluxo desta solicitação"
                      title="Ver fluxo"
                      onClick={() => setStatusFluxo(s.statusCodigo)}
                    >
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                        <circle cx="5" cy="6" r="2" />
                        <circle cx="19" cy="6" r="2" />
                        <circle cx="12" cy="18" r="2" />
                        <path d="M7 6h10" />
                        <path d="M12 8v8" />
                      </svg>
                    </button>
                  </td>
                </tr>
              ))}
              {resultado.content.length === 0 && (
                <tr>
                  <td colSpan={7}>Nenhuma solicitação encontrada.</td>
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

      <FluxoBpmnModal aberto={statusFluxo !== null} onFechar={() => setStatusFluxo(null)} statusAtual={statusFluxo ?? ""} />
    </section>
  );
}