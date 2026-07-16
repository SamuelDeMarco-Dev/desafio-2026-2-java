package br.com.samuel.documentos_academicos.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import br.com.samuel.documentos_academicos.entity.Usuario;
import br.com.samuel.documentos_academicos.enums.Perfil;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SegurancaIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired JwtService jwtService;

    /** Gera um token real para os perfis informados. */
    private String bearer(Perfil... perfis) {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setLogin("teste");
        u.setPerfis(Set.of(perfis));
        return "Bearer " + jwtService.gerarToken(u);
    }

    @Test
    void requisicaoSemTokenRetorna401() throws Exception {
        mvc.perform(get("/api/alunos"))
           .andExpect(status().isUnauthorized())
           .andExpect(jsonPath("$.erro").value("Não autenticado"));
    }

    @Test
    void loginEPublico() throws Exception {
        // body vazio -> 400 de validação prova que a requisição chegou ao controller
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
           .andExpect(status().isBadRequest());
    }

    @Test
    void perfilConsultaNaoPodeCriarAluno() throws Exception {
        mvc.perform(post("/api/alunos").header(HttpHeaders.AUTHORIZATION, bearer(Perfil.CONSULTA))
                .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Samuel\"}"))
           .andExpect(status().isForbidden())
           .andExpect(jsonPath("$.erro").value("Acesso negado"));
    }

    @Test
    void perfilConsultaPodeLerSolicitacoes() throws Exception {
        mvc.perform(get("/api/solicitacoes").header(HttpHeaders.AUTHORIZATION, bearer(Perfil.CONSULTA)))
           .andExpect(status().isOk());
    }

    @Test
    void perfilConsultaNaoPodeCriarSolicitacao() throws Exception {
        mvc.perform(post("/api/solicitacoes").header(HttpHeaders.AUTHORIZATION, bearer(Perfil.CONSULTA))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"alunoId\":1,\"cursoId\":1,\"tipoDocumentoId\":1}"))
           .andExpect(status().isForbidden());
    }

    @Test
    void perfilOperadorNaoPodeExcluirAluno() throws Exception {
        mvc.perform(delete("/api/alunos/1").header(HttpHeaders.AUTHORIZATION, bearer(Perfil.OPERADOR)))
           .andExpect(status().isForbidden());
    }

    @Test
    void adminPodeCriarAluno() throws Exception {
        mvc.perform(post("/api/alunos").header(HttpHeaders.AUTHORIZATION, bearer(Perfil.ADMIN))
                .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Samuel\"}"))
           .andExpect(status().isCreated());
    }

    @Test
    void tokenAdulteradoRetorna401() throws Exception {
        mvc.perform(get("/api/alunos").header(HttpHeaders.AUTHORIZATION, "Bearer abc.def.ghi"))
           .andExpect(status().isUnauthorized());
    }
}