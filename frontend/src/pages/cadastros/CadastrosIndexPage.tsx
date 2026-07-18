import { Link } from "react-router-dom";

const ITENS = [
  { to: "/cadastros/alunos", titulo: "Alunos", descricao: "Cadastro de alunos e situação (ativo/inativo)." },
  { to: "/cadastros/cursos", titulo: "Cursos", descricao: "Cadastro de cursos oferecidos." },
  { to: "/cadastros/tipos-documento", titulo: "Tipos de documento", descricao: "Documentos que podem ser solicitados." },
  { to: "/cadastros/status", titulo: "Status", descricao: "Estados do fluxo de solicitações." },
  { to: "/cadastros/usuarios", titulo: "Usuários", descricao: "Contas de acesso ao sistema." },
];

export function CadastrosIndexPage() {
  return (
    <section>
      <div className="page-header">
        <h1>Cadastros</h1>
      </div>
      <div className="cards-links">
        {ITENS.map((item) => (
          <Link key={item.to} to={item.to} className="card card-link">
            <span className="card-valor">{item.titulo}</span>
            <span className="card-rotulo">{item.descricao}</span>
          </Link>
        ))}
      </div>
    </section>
  );
}
