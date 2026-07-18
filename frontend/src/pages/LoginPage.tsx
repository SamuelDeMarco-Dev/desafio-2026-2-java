import { useState, type FormEvent } from "react";
import { Navigate, useLocation, useNavigate } from "react-router-dom";
import { esqueciSenha, redefinirSenha } from "../auth/authApi";
import { useAuth } from "../auth/AuthContext";
import { Modal } from "../components/Modal";
import { extrairMensagemErro } from "../services/erroApi";
import { BotaoTema } from "../tema/BotaoTema";

type EtapaRecuperacao = "solicitar" | "redefinir" | "concluida";

export function LoginPage() {
  const { isAuthenticated, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [loginInput, setLoginInput] = useState("");
  const [senha, setSenha] = useState("");
  const [erro, setErro] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  const [recuperacaoAberta, setRecuperacaoAberta] = useState(false);
  const [etapa, setEtapa] = useState<EtapaRecuperacao>("solicitar");
  const [loginRecuperacao, setLoginRecuperacao] = useState("");
  const [codigo, setCodigo] = useState("");
  const [codigoGerado, setCodigoGerado] = useState<string | null>(null);
  const [novaSenha, setNovaSenha] = useState("");
  const [confirmarSenha, setConfirmarSenha] = useState("");
  const [erroRecuperacao, setErroRecuperacao] = useState<string | null>(null);
  const [processando, setProcessando] = useState(false);

  if (isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  async function aoEnviar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setErro(null);
    setEnviando(true);
    try {
      await login({ login: loginInput, senha });
      const destino = (location.state as { from?: string } | null)?.from ?? "/";
      navigate(destino, { replace: true });
    } catch {
      setErro("Login ou senha inválidos");
    } finally {
      setEnviando(false);
    }
  }

  function abrirRecuperacao() {
    setEtapa("solicitar");
    setLoginRecuperacao(loginInput);
    setCodigo("");
    setCodigoGerado(null);
    setNovaSenha("");
    setConfirmarSenha("");
    setErroRecuperacao(null);
    setRecuperacaoAberta(true);
  }

  async function aoSolicitarCodigo(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setErroRecuperacao(null);
    setProcessando(true);
    try {
      const resposta = await esqueciSenha(loginRecuperacao.trim());
      setCodigoGerado(resposta.codigoRecuperacao);
      if (resposta.codigoRecuperacao) {
        setCodigo(resposta.codigoRecuperacao);
      }
      setEtapa("redefinir");
    } catch (e) {
      setErroRecuperacao(extrairMensagemErro(e, "Não foi possível gerar o código de recuperação."));
    } finally {
      setProcessando(false);
    }
  }

  async function aoRedefinir(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (novaSenha !== confirmarSenha) {
      setErroRecuperacao("As senhas não conferem.");
      return;
    }
    setErroRecuperacao(null);
    setProcessando(true);
    try {
      await redefinirSenha(loginRecuperacao.trim(), codigo.trim(), novaSenha);
      setEtapa("concluida");
    } catch (e) {
      setErroRecuperacao(extrairMensagemErro(e, "Não foi possível redefinir a senha."));
    } finally {
      setProcessando(false);
    }
  }

  return (
    <div className="login-page">
      <div className="login-tema">
        <BotaoTema />
      </div>

      <div className="login-card">
        <div className="login-marca">
          <span className="login-marca-icone" aria-hidden="true" />
          <span className="login-marca-texto">Central de Documentos</span>
        </div>
        <h1 className="login-subtitulo">Gerenciamento de solicitações de documentos</h1>

        <form onSubmit={aoEnviar} className="login-form">
          <label htmlFor="login">Login</label>
          <input
            id="login"
            name="login"
            type="text"
            value={loginInput}
            onChange={(e) => setLoginInput(e.target.value)}
            autoComplete="username"
            required
          />

          <label htmlFor="senha">Senha</label>
          <input
            id="senha"
            name="senha"
            type="password"
            value={senha}
            onChange={(e) => setSenha(e.target.value)}
            autoComplete="current-password"
            required
          />

          {erro && (
            <p role="alert" className="erro-banner">
              {erro}
            </p>
          )}

          <button type="submit" disabled={enviando}>
            {enviando ? "Entrando..." : "Entrar"}
          </button>

          <button type="button" className="link-esqueci-senha" onClick={abrirRecuperacao}>
            Esqueci minha senha
          </button>
        </form>
      </div>

      <Modal aberto={recuperacaoAberta} onFechar={() => setRecuperacaoAberta(false)} titulo="Recuperar senha">
        {etapa === "solicitar" && (
          <form onSubmit={aoSolicitarCodigo} className="formulario">
            <p className="texto-suave">
              Informe seu login. Um código de recuperação com validade de 15 minutos será gerado.
            </p>
            <label htmlFor="login-recuperacao">Login</label>
            <input
              id="login-recuperacao"
              value={loginRecuperacao}
              onChange={(e) => setLoginRecuperacao(e.target.value)}
              required
            />
            {erroRecuperacao && (
              <p role="alert" className="erro-banner">
                {erroRecuperacao}
              </p>
            )}
            <button type="submit" disabled={processando}>
              {processando ? "Gerando..." : "Gerar código"}
            </button>
          </form>
        )}

        {etapa === "redefinir" && (
          <form onSubmit={aoRedefinir} className="formulario">
            {codigoGerado ? (
              <p className="texto-suave">
                Código gerado: <strong>{codigoGerado}</strong> (nesta versão sem e-mail o código aparece
                aqui; em produção ele seria enviado ao seu e-mail).
              </p>
            ) : (
              <p className="texto-suave">
                Se o login existir, um código foi gerado. Informe-o abaixo com a nova senha.
              </p>
            )}
            <label htmlFor="codigo-recuperacao">Código de recuperação</label>
            <input
              id="codigo-recuperacao"
              value={codigo}
              onChange={(e) => setCodigo(e.target.value)}
              required
              maxLength={6}
            />
            <label htmlFor="nova-senha">Nova senha</label>
            <input
              id="nova-senha"
              type="password"
              value={novaSenha}
              onChange={(e) => setNovaSenha(e.target.value)}
              required
              minLength={8}
              maxLength={72}
              autoComplete="new-password"
            />
            <label htmlFor="confirmar-senha">Confirmar nova senha</label>
            <input
              id="confirmar-senha"
              type="password"
              value={confirmarSenha}
              onChange={(e) => setConfirmarSenha(e.target.value)}
              required
              minLength={8}
              maxLength={72}
              autoComplete="new-password"
            />
            {erroRecuperacao && (
              <p role="alert" className="erro-banner">
                {erroRecuperacao}
              </p>
            )}
            <button type="submit" disabled={processando}>
              {processando ? "Redefinindo..." : "Redefinir senha"}
            </button>
          </form>
        )}

        {etapa === "concluida" && (
          <div className="formulario">
            <p>Senha redefinida com sucesso. Faça login com a nova senha.</p>
            <button type="button" className="botao-primario" onClick={() => setRecuperacaoAberta(false)}>
              Voltar ao login
            </button>
          </div>
        )}
      </Modal>
    </div>
  );
}