import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "./AuthContext";
import { podeGerenciarCadastros } from "./permissoes";

/**
 * Restringe a seção a administradores. Proteção de UI: operadores e consultas
 * acessando a URL direto voltam ao início. A escrita continua barrada no
 * backend (hasRole ADMIN) mesmo se alguém contornar a interface.
 */
export function RotaAdmin() {
  const { usuario } = useAuth();
  if (!usuario || !podeGerenciarCadastros(usuario.perfis)) {
    return <Navigate to="/" replace />;
  }
  return <Outlet />;
}