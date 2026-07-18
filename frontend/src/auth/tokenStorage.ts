const CHAVE_TOKEN = "documentos-academicos:token";
const CHAVE_EXPIRA_EM = "documentos-academicos:expiresAt";

export interface SessaoArmazenada {
  token: string;
  expiresAt: number; // epoch em milissegundos
}

/** `expiraEmSegundos` é a duração devolvida pelo login (TokenResponse.expiraEm), não um epoch. */
export function salvarSessao(token: string, expiraEmSegundos: number): void {
  const expiresAt = Date.now() + expiraEmSegundos * 1000;
  localStorage.setItem(CHAVE_TOKEN, token);
  localStorage.setItem(CHAVE_EXPIRA_EM, String(expiresAt));
}

export function lerSessao(): SessaoArmazenada | null {
  const token = localStorage.getItem(CHAVE_TOKEN);
  const expiresAtBruto = localStorage.getItem(CHAVE_EXPIRA_EM);
  if (!token || !expiresAtBruto) {
    return null;
  }
  return { token, expiresAt: Number(expiresAtBruto) };
}

export function limparSessao(): void {
  localStorage.removeItem(CHAVE_TOKEN);
  localStorage.removeItem(CHAVE_EXPIRA_EM);
}

export function sessaoExpirada(sessao: SessaoArmazenada): boolean {
  return Date.now() >= sessao.expiresAt;
}