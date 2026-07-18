import { useState } from "react";
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { podeGerenciarCadastros } from "../auth/permissoes";
import { BotaoTema } from "../tema/BotaoTema";

function classeNav({ isActive }: { isActive: boolean }): string {
  return isActive ? "nav-item nav-ativo" : "nav-item";
}

function IconeInicio() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
      <path d="M9 22V12h6v10" />
    </svg>
  );
}

function IconeSolicitacoes() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
      <path d="M14 2v6h6" />
      <path d="M16 13H8" />
      <path d="M16 17H8" />
    </svg>
  );
}

function IconeCadastros() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z" />
    </svg>
  );
}

function IconeSair() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
      <path d="M16 17l5-5-5-5" />
      <path d="M21 12H9" />
    </svg>
  );
}

export function MainLayout() {
  const { usuario, logout } = useAuth();
  const navigate = useNavigate();
  const [menuAberto, setMenuAberto] = useState(false);
  const mostraCadastros = usuario ? podeGerenciarCadastros(usuario.perfis) : false;

  function aoSair() {
    logout();
    navigate("/login", { replace: true });
  }

  function fecharMenu() {
    setMenuAberto(false);
  }

  return (
    <div className="app-shell">
      <aside className={menuAberto ? "sidebar sidebar-aberta" : "sidebar"}>
        <div className="sidebar-marca">
          <span className="sidebar-marca-icone" aria-hidden="true" />
          <span>
            <strong>Central de Documentos</strong>
            <small>Gerenciamento de solicitações</small>
          </span>
        </div>

        <nav className="sidebar-nav">
          <NavLink to="/" end className={classeNav} onClick={fecharMenu}>
            <IconeInicio />
            Início
          </NavLink>
          <NavLink to="/solicitacoes" className={classeNav} onClick={fecharMenu}>
            <IconeSolicitacoes />
            Solicitações
          </NavLink>
          {mostraCadastros && (
            <NavLink to="/cadastros" className={classeNav} onClick={fecharMenu}>
              <IconeCadastros />
              Cadastros
            </NavLink>
          )}
        </nav>

        <button type="button" className="sidebar-sair" onClick={aoSair}>
          <IconeSair />
          Sair
        </button>
      </aside>

      {menuAberto && <div className="sidebar-overlay" onClick={fecharMenu} aria-hidden="true" />}

      <div className="app-principal">
        <header className="topbar">
          <button
            type="button"
            className="topbar-menu"
            aria-label="Abrir menu"
            onClick={() => setMenuAberto(true)}
          >
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" aria-hidden="true">
              <path d="M3 6h18" />
              <path d="M3 12h18" />
              <path d="M3 18h18" />
            </svg>
          </button>
          <span className="topbar-titulo">Central de Documentos</span>
          <BotaoTema />
          <span className="topbar-usuario">{usuario?.login}</span>
        </header>

        <main className="app-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}