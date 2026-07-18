import { useEffect, useState, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { podeGerenciarSolicitacoes } from "../../auth/permissoes";
import { listarAlunosAtivos, listarCursos, listarTiposDocumento, type Opcao } from "../../services/cadastrosApi";
import { extrairMensagemErro } from "../../services/erroApi";
import { criarSolicitacao, type Prioridade } from "../../services/solicitacoesApi";

const PRIORIDADES: Prioridade[] = ["URGENTE", "ALTA", "NORMAL"];

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
  const [erro, setErro] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

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
      <h1>Nova solicitação</h1>

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

        <label htmlFor="prioridade">Prioridade</label>
        <select id="prioridade" value={prioridade} onChange={(e) => setPrioridade(e.target.value as Prioridade)}>
          {PRIORIDADES.map((p) => (
            <option key={p} value={p}>
              {p}
            </option>
          ))}
        </select>

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
