import { Route, Routes } from "react-router-dom";
import { ProtectedRoute } from "./auth/ProtectedRoute";
import { MainLayout } from "./layouts/MainLayout";
import { HomePage } from "./pages/HomePage";
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
          <Route index element={<HomePage />} />
          <Route path="solicitacoes" element={<SolicitacoesListPage />} />
          <Route path="solicitacoes/nova" element={<NovaSolicitacaoPage />} />
          <Route path="solicitacoes/:id" element={<SolicitacaoDetalhePage />} />
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Route>
    </Routes>
  );
}
