import { Link, Outlet } from "react-router-dom";

export function MainLayout() {
  return (
    <div className="app-shell">
      <header className="app-header">
        <strong>Documentos Acadêmicos</strong>
        <nav>
          <Link to="/">Início</Link>
        </nav>
      </header>

      <main className="app-content">
        <Outlet />
      </main>
    </div>
  );
}
