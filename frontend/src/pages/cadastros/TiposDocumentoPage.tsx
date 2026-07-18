import {
  atualizarTipoDocumento,
  criarTipoDocumento,
  excluirTipoDocumento,
  listarTiposDocumentoPaginado,
} from "../../services/tiposDocumentoApi";
import { CadastroNomeUnicoPage } from "./CadastroNomeUnicoPage";

export function TiposDocumentoPage() {
  return (
    <CadastroNomeUnicoPage
      titulo="Tipos de documento"
      api={{
        listar: listarTiposDocumentoPaginado,
        criar: criarTipoDocumento,
        atualizar: atualizarTipoDocumento,
        excluir: excluirTipoDocumento,
      }}
    />
  );
}
