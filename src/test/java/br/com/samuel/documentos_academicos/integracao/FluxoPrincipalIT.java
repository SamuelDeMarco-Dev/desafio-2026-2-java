package br.com.samuel.documentos_academicos.integracao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Fluxo principal ponta a ponta contra PostgreSQL real: autenticação, cadastros,
 * consultas, movimentação de status e auditoria.
 *
 * <p>Os testes compartilham o banco (um container por JVM), então cada um cria os
 * próprios dados com nomes únicos em vez de depender de limpeza entre execuções.
 */
class FluxoPrincipalIT extends IntegracaoPostgresTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;

    private String token;

    /** Autentica com o administrador criado pelo AdminBootstrap no startup. */
    @BeforeEach
    void autenticar() throws Exception {
        String corpo = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                 {"login":"administrador","senha":"senha-de-integracao-123"}"""))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        token = json.readTree(corpo).get("token").asText();
    }

    private String unico(String prefixo) {
        return prefixo + "-" + System.nanoTime();
    }

    private long criar(String rota, String corpo) throws Exception {
        String resposta = mvc.perform(post(rota)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(resposta).get("id").asLong();
    }

    /** Cria aluno, curso e tipo, devolvendo o id da solicitação aberta. */
    private long novaSolicitacao() throws Exception {
        long alunoId = criar("/api/alunos", """
                {"nome":"%s","ativo":true}""".formatted(unico("Aluno")));
        long cursoId = criar("/api/cursos", """
                {"nome":"%s"}""".formatted(unico("Curso")));
        long tipoId = criar("/api/tipos-documento", """
                {"nome":"%s"}""".formatted(unico("Tipo")));
        return criar("/api/solicitacoes", """
                {"alunoId":%d,"cursoId":%d,"tipoDocumentoId":%d}"""
                .formatted(alunoId, cursoId, tipoId));
    }

    // ----- autenticação -----

    @Test
    void loginDevolveTokenUsavel() throws Exception {
        assertTrue(token != null && !token.isBlank());
        mvc.perform(get("/api/solicitacoes").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
           .andExpect(status().isOk());
    }

    @Test
    void senhaErradaNaoAutentica() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                 {"login":"administrador","senha":"senha-errada"}"""))
           .andExpect(status().isUnauthorized());
    }

    @Test
    void semTokenNaoAcessa() throws Exception {
        mvc.perform(get("/api/solicitacoes")).andExpect(status().isUnauthorized());
    }

    // ----- cadastro e consulta -----

    @Test
    void cadastraEConsultaAluno() throws Exception {
        String nome = unico("Samuel");
        long id = criar("/api/alunos", """
                {"nome":"%s","ativo":true}""".formatted(nome));

        mvc.perform(get("/api/alunos/" + id).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.nome").value(nome))
           .andExpect(jsonPath("$.ativo").value(true));
    }

    @Test
    void nomeDuplicadoDeCursoRetorna409() throws Exception {
        String nome = unico("Direito");
        criar("/api/cursos", """
                {"nome":"%s"}""".formatted(nome));

        mvc.perform(post("/api/cursos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                 {"nome":"%s"}""".formatted(nome)))
           .andExpect(status().isConflict());
    }

    /**
     * Cobre o bug de nulos sem tipo (Issue 18): no PostgreSQL um filtro ausente
     * chegava como bytea e derrubava a consulta. No H2 isso passava verde.
     */
    @Test
    void listaComFiltrosParciaisNaoQuebraNoPostgres() throws Exception {
        novaSolicitacao();

        mvc.perform(get("/api/solicitacoes").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
           .andExpect(status().isOk());
        mvc.perform(get("/api/solicitacoes?status=ABERTA").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
           .andExpect(status().isOk());
        mvc.perform(get("/api/alunos?ativo=true").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
           .andExpect(status().isOk());
        mvc.perform(get("/api/dashboard/resumo").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
           .andExpect(status().isOk());
    }

    // ----- movimentação -----

    @Test
    void movimentaSolicitacaoAteEmitida() throws Exception {
        long id = novaSolicitacao();
        long emAnalise = statusId("EM_ANALISE");
        long aprovada = statusId("APROVADA");
        long emitida = statusId("EMITIDA");

        mover(id, emAnalise).andExpect(status().isOk())
                            .andExpect(jsonPath("$.status.codigo").value("EM_ANALISE"))
                            .andExpect(jsonPath("$.dataEmissao").doesNotExist());
        mover(id, aprovada).andExpect(status().isOk());
        mover(id, emitida).andExpect(status().isOk())
                          .andExpect(jsonPath("$.dataEmissao").exists());

        // finalizada não se movimenta mais
        mover(id, statusId("ABERTA")).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void transicaoForaDoFluxoERecusada() throws Exception {
        long id = novaSolicitacao();
        mover(id, statusId("EMITIDA")).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void responsavelIncorretoERecusado() throws Exception {
        long id = novaSolicitacao();
        mvc.perform(patch("/api/solicitacoes/" + id + "/status")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                 {"statusId":%d,"codigoResponsavel":9999}""".formatted(statusId("EM_ANALISE"))))
           .andExpect(status().isForbidden());
    }

    @Test
    void historicoRegistraAberturaEMovimentacao() throws Exception {
        long id = novaSolicitacao();
        mover(id, statusId("EM_ANALISE")).andExpect(status().isOk());

        String corpo = mvc.perform(get("/api/solicitacoes/" + id + "/historico")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode historico = json.readTree(corpo);
        assertEquals(2, historico.size());
        assertTrue(historico.get(0).get("statusAnterior").isNull(), "a primeira linha é a abertura");
        assertEquals("ABERTA", historico.get(0).get("statusNovo").get("codigo").asText());
        assertEquals("EM_ANALISE", historico.get(1).get("statusNovo").get("codigo").asText());
        assertEquals("administrador", historico.get(1).get("responsavel").get("nome").asText().toLowerCase());
    }

    // ----- auditoria -----

    @Test
    void auditoriaRegistraInclusaoAlteracaoEUsuario() throws Exception {
        String nome = unico("Auditado");
        long id = criar("/api/alunos", """
                {"nome":"%s","ativo":true}""".formatted(nome));

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/api/alunos/" + id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                 {"nome":"%s"}""".formatted(nome + "-renomeado")))
           .andExpect(status().isOk());

        List<Integer> revtypes = jdbc.queryForList(
                "select a.revtype from aluno_aud a where a.id = ? order by a.rev", Integer.class, id);
        assertEquals(List.of(0, 1), revtypes, "0 = inclusão, 1 = alteração");

        String autor = jdbc.queryForObject("""
                select r.usuario_login from revinfo r
                join aluno_aud a on a.rev = r.rev
                where a.id = ? order by r.rev limit 1""", String.class, id);
        assertEquals("administrador", autor);
    }

    @Test
    void senhaNaoVaiParaTabelaDeAuditoria() {
        Integer colunas = jdbc.queryForObject("""
                select count(*) from information_schema.columns
                where table_name = 'usuario_aud' and column_name = 'senha'""", Integer.class);
        assertEquals(0, colunas, "o hash da senha não pode ser guardado no histórico");
    }

    // ----- migrations -----

    @Test
    void migrationsForamAplicadas() {
        Integer aplicadas = jdbc.queryForObject(
                "select count(*) from flyway_schema_history where success = true", Integer.class);
        assertTrue(aplicadas != null && aplicadas >= 8, "esperado V1..V8 aplicadas, veio " + aplicadas);

        String versao = jdbc.queryForObject(
                "select version from flyway_schema_history order by installed_rank desc limit 1", String.class);
        assertEquals("8", versao);
    }

    // ----- helpers -----

    private long statusId(String codigo) {
        return jdbc.queryForObject("select id from status_solicitacao where codigo = ?", Long.class, codigo);
    }

    private org.springframework.test.web.servlet.ResultActions mover(long solicitacaoId, long statusId)
            throws Exception {
        return mvc.perform(patch("/api/solicitacoes/" + solicitacaoId + "/status")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                         {"statusId":%d,"codigoResponsavel":1000}""".formatted(statusId)));
    }
}