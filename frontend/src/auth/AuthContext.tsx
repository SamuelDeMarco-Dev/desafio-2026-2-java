import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import { login as autenticar, type Credenciais } from "./authApi";
import { decodeJwt, type ClaimsToken } from "./jwt";
import { lerSessao, limparSessao, salvarSessao, sessaoExpirada } from "./tokenStorage";

export interface UsuarioAutenticado {
  login: string;
  codigoResponsavel: number | null;
  perfis: string[];
}

interface AuthContextValue {
  token: string | null;
  usuario: UsuarioAutenticado | null;
  isAuthenticated: boolean;
  login: (credenciais: Credenciais) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

/** Lê a sessão salva e já descarta se estiver expirada — evita começar "autenticado" com um token vencido. */
function tokenValidoArmazenado(): string | null {
  const sessao = lerSessao();
  if (!sessao) {
    return null;
  }
  if (sessaoExpirada(sessao)) {
    limparSessao();
    return null;
  }
  return sessao.token;
}

function paraUsuario(claims: ClaimsToken | null): UsuarioAutenticado | null {
  if (!claims) {
    return null;
  }
  return { login: claims.sub, codigoResponsavel: claims.responsavel, perfis: claims.roles };
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => tokenValidoArmazenado());
  const usuario = useMemo(() => paraUsuario(token ? decodeJwt(token) : null), [token]);

  // Sincroniza entre abas: se uma aba faz logout, as outras percebem via o evento "storage".
  useEffect(() => {
    function aoMudarStorage() {
      setToken(tokenValidoArmazenado());
    }
    window.addEventListener("storage", aoMudarStorage);
    return () => window.removeEventListener("storage", aoMudarStorage);
  }, []);

  async function login(credenciais: Credenciais) {
    const resposta = await autenticar(credenciais);
    salvarSessao(resposta.token, resposta.expiraEm);
    setToken(resposta.token);
  }

  function logout() {
    limparSessao();
    setToken(null);
  }

  return (
    <AuthContext.Provider value={{ token, usuario, isAuthenticated: token !== null, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth precisa ser usado dentro de <AuthProvider>");
  }
  return context;
}
