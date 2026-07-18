/**
 * Decodifica o payload do JWT no navegador — não valida a assinatura (isso é
 * responsabilidade exclusiva do backend, em toda requisição). Serve só para a
 * UI saber que perfil mostrar/esconder; nunca é a fonte de autorização real.
 */
export interface ClaimsToken {
  sub: string; // login
  userId: number;
  responsavel: number; // codigoResponsavel
  roles: string[];
  iat: number;
  exp: number;
}

export function decodeJwt(token: string): ClaimsToken | null {
  try {
    const payloadBase64Url = token.split(".")[1];
    const payloadBase64 = payloadBase64Url.replace(/-/g, "+").replace(/_/g, "/");
    const payloadJson = atob(payloadBase64);
    return JSON.parse(payloadJson) as ClaimsToken;
  } catch {
    return null;
  }
}
