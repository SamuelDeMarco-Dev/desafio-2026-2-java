import { atualizarCurso, criarCurso, excluirCurso, listarCursosPaginado } from "../../services/cursosApi";
import { CadastroNomeUnicoPage } from "./CadastroNomeUnicoPage";

export function CursosPage() {
  return (
    <CadastroNomeUnicoPage
      titulo="Cursos"
      api={{
        listar: listarCursosPaginado,
        criar: criarCurso,
        atualizar: atualizarCurso,
        excluir: excluirCurso,
      }}
    />
  );
}
