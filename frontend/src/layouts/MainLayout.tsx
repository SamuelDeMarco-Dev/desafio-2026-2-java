import { Link, Outlet, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

export function MainLayout() {
  const { logout } = useAuth();
  const navigate = useNavigate();

  function aoSair() {
    logout();
    navigate("/login", { replace: true });
  }

  return (
    <div className="app-shell">
      <header className="app-header">
        <strong>Documentos Acadêmicos</strong>
        <nav>
          <Link to="/">Início</Link>
          <Link to="/solicitacoes">Solicitações</Link>
          <Link to="/cadastros">Cadastros</Link>
        </nav>
        <button type="button" onClick={aoSair}>
          Sair
        </button>
      </header>

      <main className="app-content">
        <Outlet />
      </main>
    </div>
  );
}
