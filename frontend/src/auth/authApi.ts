import { api } from "../services/apiClient";

export interface Credenciais {
  login: string;
  senha: string;
}

export interface TokenResponse {
  tipo: string;
  token: string;
  /** Duração em segundos a partir da emissão — não é um epoch. */
  expiraEm: number;
}

export async function login(credenciais: Credenciais): Promise<TokenResponse> {
  const { data } = await api.post<TokenResponse>("/api/auth/login", credenciais);
  return data;
}

export interface RecuperacaoSenhaResponse {
  mensagem: string;
  /** Sem serviço de e-mail no projeto, o código vem na resposta; em produção viria nulo. */
  codigoRecuperacao: string | null;
}

export async function esqueciSenha(login: string): Promise<RecuperacaoSenhaResponse> {
  const { data } = await api.post<RecuperacaoSenhaResponse>("/api/auth/esqueci-senha", { login });
  return data;
}

export async function redefinirSenha(login: string, codigo: string, novaSenha: string): Promise<void> {
  await api.post("/api/auth/redefinir-senha", { login, codigo, novaSenha });
}