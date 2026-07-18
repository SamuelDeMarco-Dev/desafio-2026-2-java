import { useCallback, useEffect, useRef, useState, type ChangeEvent } from "react";
import { useAuth } from "../auth/AuthContext";
import { podeExcluirRecursos, podeGerenciarSolicitacoes } from "../auth/permissoes";
import {
  baixarAnexo,
  enviarAnexo,
  excluirAnexo,
  formatarTamanho,
  listarAnexos,
  type Anexo,
} from "../services/anexosApi";
import { extrairMensagemErro } from "../services/erroApi";

interface SecaoAnexosProps {
  solicitacaoId: number;
}

export function SecaoAnexos({ solicitacaoId }: SecaoAnexosProps) {
  const { usuario } = useAuth();
  const podeAnexar = usuario ? podeGerenciarSolicitacoes(usuario.perfis) : false;
  const podeExcluir = usuario ? podeExcluirRecursos(usuario.perfis) : false;

  const [anexos, setAnexos] = useState<Anexo[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);
  const inputArquivo = useRef<HTMLInputElement>(null);

  const carregar = useCallback(() => {
    setCarregando(true);
    listarAnexos(solicitacaoId)
      .then(setAnexos)
      .catch((e) => setErro(extrairMensagemErro(e, "Não foi possível carregar os anexos.")))
      .finally(() => setCarregando(false));
  }, [solicitacaoId]);

  useEffect(() => {
    carregar();
  }, [carregar]);

  async function aoEscolherArquivo(event: ChangeEvent<HTMLInputElement>) {
    const arquivo = event.target.files?.[0];
    if (!arquivo) {
      return;
    }
    setEnviando(true);
    setErro(null);
    try {
      await enviarAnexo(solicitacaoId, arquivo);
      carregar();
    } catch (e) {
      setErro(extrairMensagemErro(e, "Não foi possível enviar o arquivo."));
    } finally {
      setEnviando(false);
      // Permite reenviar o mesmo arquivo (o input não dispara change para valor repetido)
      if (inputArquivo.current) {
        inputArquivo.current.value = "";
      }
    }
  }

  async function aoBaixar(anexo: Anexo) {
    setErro(null);
    try {
      await baixarAnexo(solicitacaoId, anexo);
    } catch (e) {
      setErro(extrairMensagemErro(e, "Não foi possível baixar o arquivo."));
    }
  }

  async function aoExcluir(anexo: Anexo) {
    if (!window.confirm(`Excluir o anexo "${anexo.nomeArquivo}"?`)) {
      return;
    }
    setErro(null);
    try {
      await excluirAnexo(solicitacaoId, anexo.id);
      carregar();
    } catch (e) {
      setErro(extrairMensagemErro(e, "Não foi possível excluir o anexo."));
    }
  }

  return (
    <div className="secao-anexos">
      <div className="secao-anexos-cabecalho">
        <h2>Anexos</h2>
        {podeAnexar && (
          <>
            <input
              ref={inputArquivo}
              type="file"
              onChange={aoEscolherArquivo}
              className="input-arquivo-oculto"
              aria-label="Escolher arquivo para anexar"
            />
            <button
              type="button"
              className="botao-primario"
              disabled={enviando}
              onClick={() => inputArquivo.current?.click()}
            >
              {enviando ? "Enviando..." : "+ Anexar arquivo"}
            </button>
          </>
        )}
      </div>

      {erro && (
        <p role="alert" className="erro-banner">
          {erro}
        </p>
      )}

      {carregando && <p>Carregando anexos...</p>}

      {!carregando && anexos.length === 0 && <p className="texto-suave">Nenhum documento anexado.</p>}

      {!carregando && anexos.length > 0 && (
        <ul className="lista-anexos">
          {anexos.map((anexo) => (
            <li key={anexo.id} className="anexo-item">
              <span className="anexo-icone" aria-hidden="true">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                  <path d="M14 2v6h6" />
                </svg>
              </span>
              <span className="anexo-info">
                <span className="anexo-nome">{anexo.nomeArquivo}</span>
                <span className="anexo-meta">
                  {formatarTamanho(anexo.tamanhoBytes)} · {new Date(anexo.dataUpload).toLocaleString("pt-BR")}
                </span>
              </span>
              <span className="acoes">
                <button type="button" onClick={() => aoBaixar(anexo)}>
                  Baixar
                </button>
                {podeExcluir && (
                  <button type="button" onClick={() => aoExcluir(anexo)}>
                    Excluir
                  </button>
                )}
              </span>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}