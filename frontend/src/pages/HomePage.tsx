import { useEffect, useState } from "react";
import { api } from "../services/apiClient";

type StatusApi = "verificando" | "online" | "offline";

export function HomePage() {
  const [status, setStatus] = useState<StatusApi>("verificando");

  useEffect(() => {
    api
      .get("/actuator/health")
      .then(() => setStatus("online"))
      .catch(() => setStatus("offline"));
  }, []);

  return (
    <section>
      <h1>Sistema de Gestão de Solicitações de Documentos Acadêmicos</h1>
      <p>
        API: <strong>{status}</strong>
      </p>
    </section>
  );
}
