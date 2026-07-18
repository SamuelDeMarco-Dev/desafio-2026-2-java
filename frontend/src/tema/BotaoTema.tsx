import { useState } from "react";
import { aplicarTema, temaAtual, type Tema } from "./tema";

function IconeSol() {
  return (
    <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <circle cx="12" cy="12" r="4" />
      <path d="M12 2v2" />
      <path d="M12 20v2" />
      <path d="M4.9 4.9l1.4 1.4" />
      <path d="M17.7 17.7l1.4 1.4" />
      <path d="M2 12h2" />
      <path d="M20 12h2" />
      <path d="M6.3 17.7l-1.4 1.4" />
      <path d="M19.1 4.9l-1.4 1.4" />
    </svg>
  );
}

function IconeLua() {
  return (
    <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M21 12.8A9 9 0 1 1 11.2 3a7 7 0 0 0 9.8 9.8z" />
    </svg>
  );
}

export function BotaoTema() {
  const [tema, setTema] = useState<Tema>(() => temaAtual());

  function alternar() {
    const novo: Tema = tema === "claro" ? "escuro" : "claro";
    aplicarTema(novo);
    setTema(novo);
  }

  return (
    <button
      type="button"
      className="icone-botao"
      onClick={alternar}
      aria-label={tema === "claro" ? "Ativar modo noturno" : "Ativar modo claro"}
      title={tema === "claro" ? "Modo noturno" : "Modo claro"}
    >
      {tema === "claro" ? <IconeLua /> : <IconeSol />}
    </button>
  );
}