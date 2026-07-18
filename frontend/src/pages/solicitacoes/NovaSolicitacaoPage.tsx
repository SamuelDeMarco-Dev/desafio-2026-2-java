import { useEffect, useRef, useState, type ChangeEvent, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { podeGerenciarSolicitacoes } from "../../auth/permissoes";
import { BotaoVoltar } from "../../components/BotaoVoltar";
import { ChipSelect } from "../../components/ChipSelect";
import { enviarAnexo, formatarTamanho } from "../../services/anexosApi";
import { listarAlunosAtivos, listarCursos, listarTiposDocumento, type Opcao } from "../../services/cadastrosApi";
import { extrairMensagemErro } from "../../services/erroApi";
import { criarSolicitacao, type Prioridade } from "../../services/solicitacoesApi";

const OPCOES_PRIORIDADE: { value: Prioridade; label: string }[] = [
  { value: "NORMAL", label: "Normal" },
  { value: "ALTA", label: "Alta" },
  { value: "URGENTE", label: "Urgente" },
];

export function NovaSolicitacaoPage() {
  const { usuario } = useAuth();
  const navigate = useNavigate();

  const [alunos, setAlunos] = useState<Opcao[]>([]);
  const [cursos, setCursos] = useState<Opcao[]>([]);
  const [tipos, setTipos] = useState<Opcao[]>([]);
  const [carregandoOpcoes, setCarregandoOpcoes] = useState(true);
  const [erroOpcoes, setErroOpcoes] = useState<string | null>(null);

  const [alunoId, setAlunoId] = useState("");
  const [cursoId, setCursoId] = useState("");
  const [tipoDocumentoId, setTipoDocumentoId] = useState("");
  const [prioridade, setPrioridade] = useState<Prioridade>("NORMAL");
  const [arquivos, setArquivos] = useState<File[]>([]);
  const [erro, setErro] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);
  const inputArquivos = useRef<HTMLInputElement>(null);

  useEffect(() => {
    Promise.all([listarAlunosAtivos(), listarCursos(), listarTiposDocumento()])
      .then(([a, c, t]) => {
        setAlunos(a);
        setCursos(c);
        setTipos(t);
      })
      .catch((e) => setErroOpcoes(extrairMensagemErro(e, "Não foi possível carregar os dados do formulário.")))
      .finally(() => setCarregandoOpcoes(false));
  }, []);

  function aoEscolherArquivos(event: ChangeEvent<HTMLInputElement>) {
    const escolhidos = Array.from(event.target.files ?? []);
    if (escolhidos.length > 0) {
      setArquivos((atuais) => [...atuais, ...escolhidos]);
    }
    if (inputArquivos.current) {
      inputArquivos.current.value = "";
    }
  }

  function removerArquivo(indice: number) {
    setArquivos((atuais) => atuais.filter((_, i) => i !== indice));
  }

  async function aoEnviar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setErro(null);
    setEnviando(true);
    try {
      const nova = await criarSolicitacao({
        alunoId: Number(alunoId),
        cursoId: Number(cursoId),
        tipoDocumentoId: Number(tipoDocumentoId),
        prioridade,
      });
      // A solicitação já existe; envia os anexos escolhidos na sequência.
      for (const arquivo of arquivos) {
        await enviarAnexo(nova.id, arquivo);
      }
      navigate(`/solicitacoes/${nova.id}`, { replace: true });
    } catch (e) {
      setErro(extrairMensagemErro(e, "Não foi possível criar a solicitação."));
    } finally {
      setEnviando(false);
    }
  }

  if (!usuario || !podeGerenciarSolicitacoes(usuario.perfis)) {
    return <p>Você não tem permissão para criar solicitações.</p>;
  }

  if (carregandoOpcoes) {
    return <p>Carregando...</p>;
  }

  if (erroOpcoes) {
    return (
      <p role="alert" className="erro-banner">
        {erroOpcoes}
      </p>
    );
  }

  return (
    <section>
      <div className="page-header">
        <div className="page-header-titulo">
          <BotaoVoltar />
          <h1>Nova solicitação</h1>
        </div>
      </div>

      <form onSubmit={aoEnviar} className="formulario">
        <label htmlFor="aluno">Aluno</label>
        <select id="aluno" value={alunoId} onChange={(e) => setAlunoId(e.target.value)} required>
          <option value="" disabled>
            Selecione...
          </option>
          {alunos.map((a) => (
            <option key={a.id} value={a.id}>
              {a.nome}
            </option>
          ))}
        </select>

        <label htmlFor="curso">Curso</label>
        <select id="curso" value={cursoId} onChange={(e) => setCursoId(e.target.value)} required>
          <option value="" disabled>
            Selecione...
          </option>
          {cursos.map((c) => (
            <option key={c.id} value={c.id}>
              {c.nome}
            </option>
          ))}
        </select>

        <label htmlFor="tipoDocumento">Tipo de documento</label>
        <select id="tipoDocumento" value={tipoDocumentoId} onChange={(e) => setTipoDocumentoId(e.target.value)} required>
          <option value="" disabled>
            Selecione...
          </option>
          {tipos.map((t) => (
            <option key={t.id} value={t.id}>
              {t.nome}
            </option>
          ))}
        </select>

        <label>Prioridade</label>
        <ChipSelect options={OPCOES_PRIORIDADE} value={prioridade} onChange={setPrioridade} nomeGrupo="Prioridade" />

        <label>Documentos anexos (opcional)</label>
        <input
          ref={inputArquivos}
          type="file"
          multiple
          onChange={aoEscolherArquivos}
          className="input-arquivo-oculto"
          aria-label="Escolher arquivos para anexar"
        />
        <button type="button" className="botao-secundario" onClick={() => inputArquivos.current?.click()}>
          + Adicionar arquivos
        </button>

        {arquivos.length > 0 && (
          <ul className="lista-anexos">
            {arquivos.map((arquivo, indice) => (
              <li key={`${arquivo.name}-${indice}`} className="anexo-item">
                <span className="anexo-info">
                  <span className="anexo-nome">{arquivo.name}</span>
                  <span className="anexo-meta">{formatarTamanho(arquivo.size)}</span>
                </span>
                <span className="acoes">
                  <button type="button" onClick={() => removerArquivo(indice)}>
                    Remover
                  </button>
                </span>
              </li>
            ))}
          </ul>
        )}

        {erro && (
          <p role="alert" className="erro-banner">
            {erro}
          </p>
        )}

        <button type="submit" disabled={enviando}>
          {enviando ? "Enviando..." : "Criar solicitação"}
        </button>
      </form>
    </section>
  );
}