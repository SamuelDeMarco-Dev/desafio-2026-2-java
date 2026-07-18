import { Modal } from "./Modal";

const CODIGOS_ESTRUTURAIS = ["ABERTA", "EM_ANALISE", "APROVADA", "EMITIDA", "REPROVADA"];

interface FluxoBpmnModalProps {
  aberto: boolean;
  onFechar: () => void;
  statusAtual: string;
}

function classeNo(codigo: string, statusAtual: string): string {
  return codigo === statusAtual ? "bpmn-no bpmn-no-atual" : "bpmn-no";
}

export function FluxoBpmnModal({ aberto, onFechar, statusAtual }: FluxoBpmnModalProps) {
  const foraDoFluxoPadrao = !CODIGOS_ESTRUTURAIS.includes(statusAtual);

  return (
    <Modal aberto={aberto} onFechar={onFechar} titulo="Fluxo da solicitação">
      {foraDoFluxoPadrao && (
        <p className="bpmn-aviso">
          O status atual (<strong>{statusAtual}</strong>) é personalizado e não faz parte do fluxo padrão abaixo.
        </p>
      )}
      <div className="bpmn-scroll">
        <svg viewBox="0 0 900 260" role="img" aria-label="Diagrama do fluxo de solicitação" className="bpmn-svg">
          <defs>
            <marker id="seta" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
              <path d="M 0 0 L 10 5 L 0 10 z" fill="#94a3b8" />
            </marker>
          </defs>

          <line x1="54" y1="130" x2="85" y2="130" stroke="#94a3b8" strokeWidth="2" markerEnd="url(#seta)" />
          <line x1="195" y1="130" x2="225" y2="130" stroke="#94a3b8" strokeWidth="2" markerEnd="url(#seta)" />
          <line x1="335" y1="130" x2="388" y2="130" stroke="#94a3b8" strokeWidth="2" markerEnd="url(#seta)" />
          <path d="M 428 112 L 428 70 L 485 70" fill="none" stroke="#94a3b8" strokeWidth="2" markerEnd="url(#seta)" />
          <path d="M 428 148 L 428 190 L 485 190" fill="none" stroke="#94a3b8" strokeWidth="2" markerEnd="url(#seta)" />
          <line x1="595" y1="70" x2="635" y2="70" stroke="#94a3b8" strokeWidth="2" markerEnd="url(#seta)" />
          <line x1="745" y1="70" x2="782" y2="70" stroke="#94a3b8" strokeWidth="2" markerEnd="url(#seta)" />
          <line x1="595" y1="190" x2="672" y2="190" stroke="#94a3b8" strokeWidth="2" markerEnd="url(#seta)" />

          <circle cx="40" cy="130" r="14" fill="#3fa66a" />

          <g className={classeNo("ABERTA", statusAtual)}>
            <rect x="85" y="105" width="110" height="50" rx="10" />
            <text x="140" y="134" textAnchor="middle">ABERTA</text>
          </g>

          <g className={classeNo("EM_ANALISE", statusAtual)}>
            <rect x="225" y="105" width="110" height="50" rx="10" />
            <text x="280" y="134" textAnchor="middle">EM ANÁLISE</text>
          </g>

          <polygon points="410,105 435,130 410,155 385,130" fill="#f0923d" stroke="#c96f26" strokeWidth="1.5" />

          <g className={classeNo("APROVADA", statusAtual)}>
            <rect x="485" y="45" width="110" height="50" rx="10" />
            <text x="540" y="74" textAnchor="middle">APROVADA</text>
          </g>

          <g className={classeNo("REPROVADA", statusAtual)}>
            <rect x="485" y="165" width="110" height="50" rx="10" />
            <text x="540" y="194" textAnchor="middle">REPROVADA</text>
          </g>

          <g className={classeNo("EMITIDA", statusAtual)}>
            <rect x="635" y="45" width="110" height="50" rx="10" />
            <text x="690" y="74" textAnchor="middle">EMITIDA</text>
          </g>

          <circle cx="798" cy="70" r="14" fill="#3fa66a" stroke="#2f8556" strokeWidth="3" />
          <circle cx="690" cy="190" r="14" fill="#dc2626" />
        </svg>
      </div>
      <p className="bpmn-legenda">
        <span className="bpmn-legenda-item">
          <span className="bpmn-marcador bpmn-marcador-atual" /> etapa atual
        </span>
        <span className="bpmn-legenda-item">
          <span className="bpmn-marcador bpmn-marcador-fim-ok" /> conclusão (emitida)
        </span>
        <span className="bpmn-legenda-item">
          <span className="bpmn-marcador bpmn-marcador-fim-rejeicao" /> encerramento (reprovada)
        </span>
      </p>
    </Modal>
  );
}
