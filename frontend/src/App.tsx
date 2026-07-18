import { Route, Routes } from "react-router-dom";
import { ProtectedRoute } from "./auth/ProtectedRoute";
import { MainLayout } from "./layouts/MainLayout";
import { DashboardPage } from "./pages/dashboard/DashboardPage";
import { AlunosPage } from "./pages/cadastros/AlunosPage";
import { CadastrosIndexPage } from "./pages/cadastros/CadastrosIndexPage";
import { CursosPage } from "./pages/cadastros/CursosPage";
import { StatusPage } from "./pages/cadastros/StatusPage";
import { TiposDocumentoPage } from "./pages/cadastros/TiposDocumentoPage";
import { UsuariosPage } from "./pages/cadastros/UsuariosPage";
import { LoginPage } from "./pages/LoginPage";
import { NotFoundPage } from "./pages/NotFoundPage";
import { NovaSolicitacaoPage } from "./pages/solicitacoes/NovaSolicitacaoPage";
import { SolicitacaoDetalhePage } from "./pages/solicitacoes/SolicitacaoDetalhePage";
import { SolicitacoesListPage } from "./pages/solicitacoes/SolicitacoesListPage";

export function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      <Route element={<ProtectedRoute />}>
        <Route path="/" element={<MainLayout />}>
          <Route index element={<DashboardPage />} />
          <Route path="solicitacoes" element={<SolicitacoesListPage />} />
          <Route path="solicitacoes/nova" element={<NovaSolicitacaoPage />} />
          <Route path="solicitacoes/:id" element={<SolicitacaoDetalhePage />} />
          <Route path="cadastros" element={<CadastrosIndexPage />} />
          <Route path="cadastros/alunos" element={<AlunosPage />} />
          <Route path="cadastros/cursos" element={<CursosPage />} />
          <Route path="cadastros/tipos-documento" element={<TiposDocumentoPage />} />
          <Route path="cadastros/status" element={<StatusPage />} />
          <Route path="cadastros/usuarios" element={<UsuariosPage />} />
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Route>
    </Routes>
  );
}
