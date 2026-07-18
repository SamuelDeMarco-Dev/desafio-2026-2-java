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

  // Já logado (ex.: voltou para /login manualmente) — não faz sentido mostrar o formulário.
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
      // A API responde a mesma mensagem genérica para login inexistente, senha
      // errada ou usuário inativo, para não permitir enumeração de usuários.
      setErro("Login ou senha inválidos");
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div className="login-page">
      <form onSubmit={aoEnviar} className="login-form">
        <h1>Entrar</h1>

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
  );
}