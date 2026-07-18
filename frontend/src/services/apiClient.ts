import axios from "axios";
import { lerSessao, limparSessao, sessaoExpirada } from "../auth/tokenStorage";

const baseURL = import.meta.env.VITE_API_URL;

if (!baseURL) {
  // Falha cedo: melhor que um cliente apontando para undefined, que devolve
  // erros de rede difíceis de diagnosticar mais adiante.
  throw new Error(
    "VITE_API_URL não definida. Copie frontend/.env.example para frontend/.env e ajuste o valor."
  );
}

export const api = axios.create({
  baseURL,
  headers: {
    "Content-Type": "application/json",
  },
});

// Rotas que a API expõe sem autenticação (SegurancaConfig.permitAll). O
// JwtAuthenticationFilter do backend rejeita com 401 QUALQUER requisição cujo
// header Authorization seja inválido/expirado, mesmo em rota pública — por
// isso nunca anexamos um token velho aqui, ou a própria tela de login quebraria.
const ROTAS_PUBLICAS = ["/api/auth/login", "/api/auth/esqueci-senha", "/api/auth/redefinir-senha"];

api.interceptors.request.use((config) => {
  const rotaPublica = ROTAS_PUBLICAS.some((rota) => config.url?.startsWith(rota));
  if (rotaPublica) {
    return config;
  }

  const sessao = lerSessao();
  if (sessao && !sessaoExpirada(sessao)) {
    config.headers.Authorization = `Bearer ${sessao.token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      limparSessao();
      // Reload completo: o interceptor roda fora da árvore do React, então não
      // há acesso direto ao navigate() do Router. O reload também garante que o
      // AuthContext reinicialize lendo o localStorage já limpo.
      if (window.location.pathname !== "/login") {
        window.location.assign("/login");
      }
    }
    return Promise.reject(error);
  }
);