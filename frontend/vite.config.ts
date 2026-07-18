import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,       // casa com o CORS_ORIGENS padrão do backend
    strictPort: true,  // falha em vez de subir silenciosamente em outra porta
  },
});
