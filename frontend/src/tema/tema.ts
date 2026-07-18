export type Tema = "claro" | "escuro";

const CHAVE_STORAGE = "tema";

export function temaAtual(): Tema {
  const salvo = localStorage.getItem(CHAVE_STORAGE);
  if (salvo === "claro" || salvo === "escuro") {
    return salvo;
  }
  // Primeira visita: parte da preferência do sistema operacional.
  return window.matchMedia("(prefers-color-scheme: dark)").matches ? "escuro" : "claro";
}

export function aplicarTema(tema: Tema): void {
  document.documentElement.setAttribute("data-tema", tema);
  localStorage.setItem(CHAVE_STORAGE, tema);
}

/** Chamado uma vez no bootstrap, antes do primeiro render, para evitar "flash" de tema errado. */
export function aplicarTemaInicial(): void {
  document.documentElement.setAttribute("data-tema", temaAtual());
}