import { useCallback, useEffect, useState, type FormEvent } from "react";
import { useAuth } from "../../auth/AuthContext";
import { podeGerenciarCadastros } from "../../auth/permissoes";
import { extrairErrosDeCampo, extrairMensagemErro } from "../../services/erroApi";
import type { PageResponse } from "../../services/solicitacoesApi";
import {
  alterarSituacaoUsuario,
  atualizarUsuario,
  criarUsuario,
  listarUsuariosPaginado,
  type Perfil,
  type Usuario,
} from "../../services/usuariosApi";

const PERFIS: Perfil[] = ["ADMIN", "OPERADOR", "CONSULTA"];

interface FiltroForm {
  nome: string;
  ativo: string;
}

const FILTRO_VAZIO: FiltroForm = { nome: "", ativo: "" };

interface UsuarioForm {
  nome: string;
  login: string;
  senha: string;
  codigoResponsavel: string;
  perfis: Perfil[];
}

const FORM_VAZIO: UsuarioForm = { nome: "", login: "", senha: "", codigoResponsavel: "", perfis: [] };

export function UsuariosPage() {
  const { usuario } = useAuth();
  const gerencia = usuario ? podeGerenciarCadastros(usuario.perfis) : false;

  const [filtroForm, setFiltroForm] = useState<FiltroForm>(FILTRO_VAZIO);
  const [filtroAplicado, setFiltroAplicado] = useState<FiltroForm>(FILTRO_VAZIO);
  const [pagina, setPagina] = useState(0);
  const [resultado, setResultado] = useState<PageResponse<Usuario> | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);

  const [mostrarForm, setMostrarForm] = useState(false);
  const [editandoId, setEditandoId] = useState<number | null>(null);
  const [form, setForm] = useState<UsuarioForm>(FORM_VAZIO);
  const [errosCampo, setErrosCampo] = useState<Record<string, string>>({});
  const [salvando, setSalvando] = useState(false);

  const carregar = useCallback(() => {
    setCarregando(true);
    setErro(null);
    listarUsuariosPaginado(
      {
        nome: filtroAplicado.nome.trim() || undefined,
        ativo: filtroAplicado.ativo === "" ? undefined : filtroAplicado.ativo === "true",
      },
      pagina
    )
      .then(setResultado)
      .catch((e) => setErro(extrairMensagemErro(e, "Não foi possível carregar os usuários.")))
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
    setForm(FORM_VAZIO);
    setErrosCampo({});
    setMostrarForm(true);
  }

  function aoEditar(item: Usuario) {
    setEditandoId(item.id);
    setForm({
      nome: item.nome,
      login: item.login,
      senha: "",
      codigoResponsavel: item.codigoResponsavel !== null ? String(item.codigoResponsavel) : "",
      perfis: item.perfis,
    });
    setErrosCampo({});
    setMostrarForm(true);
  }

  function alternarPerfil(perfil: Perfil) {
    setForm((atual) => ({
      ...atual,
      perfis: atual.perfis.includes(perfil) ? atual.perfis.filter((p) => p !== perfil) : [...atual.perfis, perfil],
    }));
  }

  async function aoSalvar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSalvando(true);
    setErro(null);
    setErrosCampo({});
    try {
      const codigoResponsavel = form.codigoResponsavel.trim() ? Number(form.codigoResponsavel) : null;
      if (editandoId !== null) {
        await atualizarUsuario(editandoId, { nome: form.nome, codigoResponsavel, perfis: form.perfis });
      } else {
        await criarUsuario({
          nome: form.nome,
          login: form.login,
          senha: form.senha,
          codigoResponsavel,
          perfis: form.perfis,
        });
      }
      setMostrarForm(false);
      carregar();
    } catch (e) {
      setErro(extrairMensagemErro(e, "Não foi possível salvar o usuário."));
      setErrosCampo(extrairErrosDeCampo(e));
    } finally {
      setSalvando(false);
    }
  }

  async function aoAlterarSituacao(item: Usuario) {
    setErro(null);
    try {
      await alterarSituacaoUsuario(item.id, !item.ativo);
      carregar();
    } catch (e) {
      setErro(extrairMensagemErro(e, "Não foi possível alterar a situação do usuário."));
    }
  }

  return (
    <section>
      <div className="page-header">
        <h1>Usuários</h1>
        {gerencia && !mostrarForm && (
          <button type="button" className="botao-primario" onClick={aoNovo}>
            Novo usuário
          </button>
        )}
      </div>

      <form onSubmit={aoFiltrar} className="filtros">
        <input
          placeholder="Nome"
          value={filtroForm.nome}
          onChange={(e) => setFiltroForm({ ...filtroForm, nome: e.target.value })}
        />
        <select value={filtroForm.ativo} onChange={(e) => setFiltroForm({ ...filtroForm, ativo: e.target.value })}>
          <option value="">Ativos e inativos</option>
          <option value="true">Somente ativos</option>
          <option value="false">Somente inativos</option>
        </select>
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
          <label htmlFor="nome-usuario">Nome</label>
          <input
            id="nome-usuario"
            value={form.nome}
            onChange={(e) => setForm({ ...form, nome: e.target.value })}
            required
            maxLength={150}
          />
          {errosCampo.nome && <p className="erro-campo">{errosCampo.nome}</p>}

          <label htmlFor="login-usuario">Login</label>
          <input
            id="login-usuario"
            value={form.login}
            onChange={(e) => setForm({ ...form, login: e.target.value })}
            required
            maxLength={50}
            disabled={editandoId !== null}
            title={editandoId !== null ? "O login não pode ser alterado." : undefined}
          />
          {errosCampo.login && <p className="erro-campo">{errosCampo.login}</p>}

          {editandoId === null && (
            <>
              <label htmlFor="senha-usuario">Senha</label>
              <input
                id="senha-usuario"
                type="password"
                value={form.senha}
                onChange={(e) => setForm({ ...form, senha: e.target.value })}
                required
                minLength={8}
                maxLength={72}
              />
              {errosCampo.senha && <p className="erro-campo">{errosCampo.senha}</p>}
            </>
          )}

          <label htmlFor="responsavel-usuario">Código do responsável (opcional)</label>
          <input
            id="responsavel-usuario"
            type="number"
            value={form.codigoResponsavel}
            onChange={(e) => setForm({ ...form, codigoResponsavel: e.target.value })}
          />
          {errosCampo.codigoResponsavel && <p className="erro-campo">{errosCampo.codigoResponsavel}</p>}

          <fieldset>
            <legend>Perfis</legend>
            {PERFIS.map((perfil) => (
              <label key={perfil} className="opcao-checkbox">
                <input type="checkbox" checked={form.perfis.includes(perfil)} onChange={() => alternarPerfil(perfil)} />
                {perfil}
              </label>
            ))}
          </fieldset>
          {errosCampo.perfis && <p className="erro-campo">{errosCampo.perfis}</p>}

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
                <th>Login</th>
                <th>Perfis</th>
                <th>Situação</th>
                {gerencia && <th>Ações</th>}
              </tr>
            </thead>
            <tbody>
              {resultado.content.map((item) => (
                <tr key={item.id}>
                  <td>{item.nome}</td>
                  <td>{item.login}</td>
                  <td>{item.perfis.join(", ")}</td>
                  <td>{item.ativo ? "Ativo" : "Inativo"}</td>
                  {gerencia && (
                    <td className="acoes">
                      <button type="button" onClick={() => aoEditar(item)}>
                        Editar
                      </button>
                      <button type="button" onClick={() => aoAlterarSituacao(item)}>
                        {item.ativo ? "Inativar" : "Ativar"}
                      </button>
                    </td>
                  )}
                </tr>
              ))}
              {resultado.content.length === 0 && (
                <tr>
                  <td colSpan={gerencia ? 5 : 4}>Nenhum usuário encontrado.</td>
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
