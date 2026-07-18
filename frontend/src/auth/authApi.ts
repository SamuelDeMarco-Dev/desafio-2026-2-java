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