import { useCallback, useEffect, useState, type FormEvent } from "react";
import { useAuth } from "../../auth/AuthContext";
import { BotaoVoltar } from "../../components/BotaoVoltar";
import { ChipSelect } from "../../components/ChipSelect";
import { podeGerenciarCadastros } from "../../auth/permissoes";
import {
  alterarSituacaoAluno,
  atualizarAluno,
  criarAluno,
  excluirAluno,
  listarAlunosPaginado,
  type Aluno,
} from "../../services/alunosApi";
import { extrairErrosDeCampo, extrairMensagemErro } from "../../services/erroApi";
import type { PageResponse } from "../../services/solicitacoesApi";

interface FiltroForm {
  nome: string;
  ativo: string;
}

const FILTRO_VAZIO: FiltroForm = { nome: "", ativo: "" };

export function AlunosPage() {
  const { usuario } = useAuth();
  const gerencia = usuario ? podeGerenciarCadastros(usuario.perfis) : false;

  const [filtroForm, setFiltroForm] = useState<FiltroForm>(FILTRO_VAZIO);
  const [filtroAplicado, setFiltroAplicado] = useState<FiltroForm>(FILTRO_VAZIO);
  const [pagina, setPagina] = useState(0);
  const [resultado, setResultado] = useState<PageResponse<Aluno> | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);

  const [mostrarForm, setMostrarForm] = useState(false);
  const [editandoId, setEditandoId] = useState<number | null>(null);
  const [nomeForm, setNomeForm] = useState("");
  const [errosCampo, setErrosCampo] = useState<Record<string, string>>({});
  const [salvando, setSalvando] = useState(false);

  const carregar = useCallback(() => {
    setCarregando(true);
    setErro(null);
    listarAlunosPaginado(
      {
        nome: filtroAplicado.nome.trim() || undefined,
        ativo: filtroAplicado.ativo === "" ? undefined : filtroAplicado.ativo === "true",
      },
      pagina
    )
      .then(setResultado)
      .catch((e) => setErro(extrairMensagemErro(e, "Não foi possível carregar os alunos.")))
      .finally(() => setCarregando(false));
  }, [filtroAplicado, pagina]);

  useEffect(() => {
    carregar();
  }, [carregar]);

  function aoFiltrar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setPagina(0);
    setFiltroAplicado(filtroForm);
  }

  function aoLimparFiltro() {
    setFiltroForm(FILTRO_VAZIO);
    setPagina(0);
    setFiltroAplicado(FILTRO_VAZIO);
  }

  function aoNovo() {
    setEditandoId(null);
    setNomeForm("");
    setErrosCampo({});
    setMostrarForm(true);
  }

  function aoEditar(aluno: Aluno) {
    setEditandoId(aluno.id);
    setNomeForm(aluno.nome);
    setErrosCampo({});
    setMostrarForm(true);
  }

  async function aoSalvar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSalvando(true);
    setErro(null);
    setErrosCampo({});
    try {
      if (editandoId !== null) {
        await atualizarAluno(editandoId, nomeForm);
      } else {
        await criarAluno(nomeForm);
      }
      setMostrarForm(false);
      carregar();
    } catch (e) {
      setErro(extrairMensagemErro(e, "Não foi possível salvar o aluno."));
      setErrosCampo(extrairErrosDeCampo(e));
    } finally {
      setSalvando(false);
    }
  }

  async function aoAlterarSituacao(aluno: Aluno) {
    setErro(null);
    try {
      await alterarSituacaoAluno(aluno.id, !aluno.ativo);
      carregar();
    } catch (e) {
      setErro(extrairMensagemErro(e, "Não foi possível alterar a situação do aluno."));
    }
  }

  async function aoExcluir(aluno: Aluno) {
    if (!window.confirm(`Excluir "${aluno.nome}"?`)) {
      return;
    }
    setErro(null);
    try {
      await excluirAluno(aluno.id);
      carregar();
    } catch (e) {
      setErro(extrairMensagemErro(e, "Não foi possível excluir o aluno."));
    }
  }

  return (
    <section>
      <div className="page-header">
        <div className="page-header-titulo">
          <BotaoVoltar />
          <h1>Alunos</h1>
        </div>
        {gerencia && !mostrarForm && (
          <button type="button" className="botao-primario" onClick={aoNovo}>
            Novo aluno
          </button>
        )}
      </div>

      <form onSubmit={aoFiltrar} className="filtros">
        <input
          placeholder="Nome"
          value={filtroForm.nome}
          onChange={(e) => setFiltroForm({ ...filtroForm, nome: e.target.value })}
        />
        <ChipSelect
            options={[
                { value: "", label: "Ativos e inativos" },
                { value: "true", label: "Somente ativos" },
                { value: "false", label: "Somente inativos" },
            ]}
            value={filtroForm.ativo}
            onChange={(v) => setFiltroForm({ ...filtroForm, ativo: v })}
            nomeGrupo="Situação"
        />


        <button type="submit">Filtrar</button>
        <button type="button" onClick={aoLimparFiltro}>
          Limpar
        </button>
      </form>

      {erro && (
        <p role="alert" className="erro-banner">
          {erro}
        </p>
      )}

      {gerencia && mostrarForm && (
        <form onSubmit={aoSalvar} className="formulario">
          <label htmlFor="nome-aluno">Nome</label>
          <input
            id="nome-aluno"
            value={nomeForm}
            onChange={(e) => setNomeForm(e.target.value)}
            required
            maxLength={150}
          />
          {errosCampo.nome && <p className="erro-campo">{errosCampo.nome}</p>}

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

      {!carregando && resultado && (
        <>
          <table className="tabela">
            <thead>
              <tr>
                <th>Nome</th>
                <th>Situação</th>
                {gerencia && <th>Ações</th>}
              </tr>
            </thead>
            <tbody>
              {resultado.content.map((aluno) => (
                <tr key={aluno.id}>
                  <td>{aluno.nome}</td>
                  <td>{aluno.ativo ? "Ativo" : "Inativo"}</td>
                  {gerencia && (
                    <td className="acoes">
                      <button type="button" onClick={() => aoEditar(aluno)}>
                        Editar
                      </button>
                      <button type="button" onClick={() => aoAlterarSituacao(aluno)}>
                        {aluno.ativo ? "Inativar" : "Ativar"}
                      </button>
                      <button type="button" onClick={() => aoExcluir(aluno)}>
                        Excluir
                      </button>
                    </td>
                  )}
                </tr>
              ))}
              {resultado.content.length === 0 && (
                <tr>
                  <td colSpan={gerencia ? 3 : 2}>Nenhum aluno encontrado.</td>
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
