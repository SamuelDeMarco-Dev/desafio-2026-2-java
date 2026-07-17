import axios from "axios";

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

// O interceptor que anexa o token JWT (Authorization: Bearer) e trata 401
// entra numa issue futura, junto com a tela de login.
