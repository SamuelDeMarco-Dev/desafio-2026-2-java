package br.com.samuel.documentos_academicos.integracao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManagerFactory;

/**
 * Prova que a listagem de solicitações não faz N+1.
 *
 * <p>O resumo lê nome do aluno, do curso, do tipo e o código do status — todos
 * {@code @ManyToOne(LAZY)}. Sem um fetch conjunto, uma página de N linhas dispara
 * 1 + 4N consultas. Aqui contamos as consultas de verdade, em vez de confiar na
 * inspeção visual do código.
 */
class ConsultasNMaisUmIT extends IntegracaoPostgresTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired EntityManagerFactory emf;

    private String token;

    @BeforeEach
    void autenticar() throws Exception {
        String corpo = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                 {"login":"administrador","senha":"senha-de-integracao-123"}"""))
                .andReturn().getResponse().getContentAsString();
        token = json.readTree(corpo).get("token").asText();
    }

    private Statistics estatisticas() {
        return emf.unwrap(SessionFactory.class).getStatistics();
    }

    private long criar(String rota, String corpo) throws Exception {
        String r = mvc.perform(post(rota)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(r).get("id").asLong();
    }

    @Test
    void listagemDeSolicitacoesNaoFazNMaisUm() throws Exception {
        long cursoId = criar("/api/cursos", """
                {"nome":"Curso N1 %d"}""".formatted(System.nanoTime()));
        long tipoId = criar("/api/tipos-documento", """
                {"nome":"Tipo N1 %d"}""".formatted(System.nanoTime()));

        // alunos distintos: cada solicitação aponta para um aluno diferente,
        // que é o caso em que o N+1 realmente dispara
        for (int i = 0; i < 5; i++) {
            long alunoId = criar("/api/alunos", """
                    {"nome":"Aluno N1 %d","ativo":true}""".formatted(System.nanoTime()));
            criar("/api/solicitacoes", """
                    {"alunoId":%d,"cursoId":%d,"tipoDocumentoId":%d}"""
                    .formatted(alunoId, cursoId, tipoId));
        }

        Statistics stats = estatisticas();
        stats.setStatisticsEnabled(true);
        stats.clear();

        mvc.perform(get("/api/solicitacoes?size=5")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
           .andExpect(status().isOk());

        long consultas = stats.getPrepareStatementCount();
        // 1 para a página + 1 para o count da paginação. Sem o fetch conjunto
        // seriam ~22 (1 + 4 relações × 5 linhas + count).
        assertTrue(consultas <= 3,
                "listagem de 5 solicitações deveria custar poucas consultas, custou " + consultas);
    }

    @Test
    void solicitacoesDoAlunoNaoFazemNMaisUm() throws Exception {
        long alunoId = criar("/api/alunos", """
                {"nome":"Aluno N1 aluno %d","ativo":true}""".formatted(System.nanoTime()));
        long cursoId = criar("/api/cursos", """
                {"nome":"Curso N1 aluno %d"}""".formatted(System.nanoTime()));
        long tipoId = criar("/api/tipos-documento", """
                {"nome":"Tipo N1 aluno %d"}""".formatted(System.nanoTime()));
        for (int i = 0; i < 5; i++) {
            criar("/api/solicitacoes", """
                    {"alunoId":%d,"cursoId":%d,"tipoDocumentoId":%d}"""
                    .formatted(alunoId, cursoId, tipoId));
        }

        Statistics stats = estatisticas();
        stats.setStatisticsEnabled(true);
        stats.clear();

        mvc.perform(get("/api/alunos/" + alunoId + "/solicitacoes?size=5")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
           .andExpect(status().isOk());

        long consultas = stats.getPrepareStatementCount();
        assertTrue(consultas <= 4,
                "solicitações do aluno deveriam custar poucas consultas, custou " + consultas);
    }

    @Test
    void historicoNaoFazNMaisUm() throws Exception {
        long alunoId = criar("/api/alunos", """
                {"nome":"Aluno hist %d","ativo":true}""".formatted(System.nanoTime()));
        long cursoId = criar("/api/cursos", """
                {"nome":"Curso hist %d"}""".formatted(System.nanoTime()));
        long tipoId = criar("/api/tipos-documento", """
                {"nome":"Tipo hist %d"}""".formatted(System.nanoTime()));
        long id = criar("/api/solicitacoes", """
                {"alunoId":%d,"cursoId":%d,"tipoDocumentoId":%d}"""
                .formatted(alunoId, cursoId, tipoId));

        Statistics stats = estatisticas();
        stats.setStatisticsEnabled(true);
        stats.clear();

        mvc.perform(get("/api/solicitacoes/" + id + "/historico")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
           .andExpect(status().isOk());

        // o @EntityGraph do HistoricoStatusRepository já cobre este caso (Issue 20)
        assertEquals(2, stats.getPrepareStatementCount(),
                "esperado: 1 exists + 1 select com o grafo carregado");
    }
}