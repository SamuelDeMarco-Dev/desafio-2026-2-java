import { useState, type FormEvent } from "react";
import type { PeriodoFiltro } from "../../services/dashboardApi";

interface FiltroPeriodoProps {
  valorAplicado: PeriodoFiltro;
  aoAplicar: (periodo: PeriodoFiltro) => void;
}

export function FiltroPeriodo({ valorAplicado, aoAplicar }: FiltroPeriodoProps) {
  const [dataInicio, setDataInicio] = useState(valorAplicado.dataInicio ?? "");
  const [dataFim, setDataFim] = useState(valorAplicado.dataFim ?? "");

  function aoEnviar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    aoAplicar({ dataInicio: dataInicio || undefined, dataFim: dataFim || undefined });
  }

  function limpar() {
    setDataInicio("");
    setDataFim("");
    aoAplicar({});
  }

  return (
    <form onSubmit={aoEnviar} className="filtros">
      <input
        type="date"
        aria-label="Data inicial"
        value={dataInicio}
        onChange={(e) => setDataInicio(e.target.value)}
      />
      <input type="date" aria-label="Data final" value={dataFim} onChange={(e) => setDataFim(e.target.value)} />
      <button type="submit">Filtrar</button>
      <button type="button" onClick={limpar}>
        Limpar
      </button>
    </form>
  );
}
