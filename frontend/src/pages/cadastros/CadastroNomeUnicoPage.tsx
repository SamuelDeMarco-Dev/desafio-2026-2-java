import { useCallback, useEffect, useState, type FormEvent } from "react";
import { useAuth } from "../../auth/AuthContext";
import { podeGerenciarCadastros } from "../../auth/permissoes";
import { extrairErrosDeCampo, extrairMensagemErro } from "../../services/erroApi";
import type { PageResponse } from "../../services/solicitacoesApi";

export interface ItemNomeUnico {
  id: number;
  nome: string;
}

export interface ApiNomeUnico {
  listar(nome: string | undefined, page: number): Promise<PageResponse<ItemNomeUnico>>;
  criar(nome: string): Promise<ItemNomeUnico>;
  atualizar(id: number, nome: string): Promise<ItemNomeUnico>;
  excluir(id: number): Promise<void>;
}

interface CadastroNomeUnicoPageProps {
  titulo: string;
  api: ApiNomeUnico;
}

export function CadastroNomeUnicoPage({ titulo, api }: CadastroNomeUnicoPageProps) {
  const { usuario } = useAuth();
  const gerencia = usuario ? podeGerenciarCadastros(usuario.perfis) : false;

  const [filtroNome, setFiltroNome] = useState("");
  const [filtroAplicado, setFiltroAplicado] = useState("");
  const [pagina, setPagina] = useState(0);
  const [resultado, setResultado] = useState<PageResponse<ItemNomeUnico> | null>(null);
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
    api
      .listar(filtroAplicado || undefined, pagina)
      .then(setResultado)
      .catch((e) => setErro(extrairMensagemErro(e, `Não foi possível carregar ${titulo.toLowerCase()}.`)))
      .finally(() => setCarregando(false));
  }, [api, filtroAplicado, pagina, titulo]);

  useEffect(() => {
    carregar();
  }, [carregar]);

  function aoFiltrar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setPagina(0);
    setFiltroAplicado(filtroNome.trim());
  }

  function aoLimparFiltro() {
    setFiltroNome("");
    setPagina(0);
    setFiltroAplicado("");
  }

  function aoNovo() {
    setEditandoId(null);
    setNomeForm("");
    setErrosCampo({});
    setMostrarForm(true);
  }

  function aoEditar(item: ItemNomeUnico) {
    setEditandoId(item.id);
    setNomeForm(item.nome);
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
        await api.atualizar(editandoId, nomeForm);
      } else {
        await api.criar(nomeForm);
      }
      setMostrarForm(false);
      carregar();
    } catch (e) {
      setErro(extrairMensagemErro(e, "Não foi possível salvar."));
      setErrosCampo(extrairErrosDeCampo(e));
    } finally {
      setSalvando(false);
    }
  }

  async function aoExcluir(item: ItemNomeUnico) {
    if (!window.confirm(`Excluir "${item.nome}"?`)) {
      return;
    }
    setErro(null);
    try {
      await api.excluir(item.id);
      carregar();
    } catch (e) {
      setErro(extrairMensagemErro(e, "Não foi possível excluir."));
    }
  }

  return (
    <section>
      <div className="page-header">
        <h1>{titulo}</h1>
        {gerencia && !mostrarForm && (
          <button type="button" className="botao-primario" onClick={aoNovo}>
            Novo
          </button>
        )}
      </div>

      <form onSubmit={aoFiltrar} className="filtros">
        <input placeholder="Nome" value={filtroNome} onChange={(e) => setFiltroNome(e.target.value)} />
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
          <label htmlFor="nome">Nome</label>
          <input id="nome" value={nomeForm} onChange={(e) => setNomeForm(e.target.value)} required maxLength={150} />
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
                {gerencia && <th>Ações</th>}
              </tr>
            </thead>
            <tbody>
              {resultado.content.map((item) => (
                <tr key={item.id}>
                  <td>{item.nome}</td>
                  {gerencia && (
                    <td className="acoes">
                      <button type="button" onClick={() => aoEditar(item)}>
                        Editar
                      </button>
                      <button type="button" onClick={() => aoExcluir(item)}>
                        Excluir
                      </button>
                    </td>
                  )}
                </tr>
              ))}
              {resultado.content.length === 0 && (
                <tr>
                  <td colSpan={gerencia ? 2 : 1}>Nenhum registro encontrado.</td>
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
