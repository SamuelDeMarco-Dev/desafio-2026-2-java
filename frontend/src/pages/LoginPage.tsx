import { useState, type FormEvent } from "react";
import { Navigate, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

export function LoginPage() {
  const { isAuthenticated, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [loginInput, setLoginInput] = useState("");
  const [senha, setSenha] = useState("");
  const [erro, setErro] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

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

  return (
    <div className="login-page">
      <div className="login-card">
        <div className="login-marca">
          <span className="login-marca-icone" aria-hidden="true" />
          <span className="login-marca-texto">UNOESC</span>
        </div>
        <h1 className="login-subtitulo">Sistema de Solicitações de Documentos Acadêmicos</h1>

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
        </form>
      </div>
    </div>
  );
}
