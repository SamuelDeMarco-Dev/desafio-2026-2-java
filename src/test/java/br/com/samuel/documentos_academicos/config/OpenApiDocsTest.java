package br.com.samuel.documentos_academicos.config;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Sem addFilters = false: aqui a cadeia de segurança real precisa participar,
 * senão os testes passariam mesmo com o permitAll da documentação esquecido.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiDocsTest {

    @Autowired MockMvc mockMvc;

    @Test
    void apiDocsAcessivelSemToken() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.components.securitySchemes['bearer-jwt'].scheme").value("bearer"))
               .andExpect(jsonPath("$.components.securitySchemes['bearer-jwt'].bearerFormat").value("JWT"));
    }

    @Test
    void swaggerUiAcessivelSemToken() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }

    @Test
    void loginDocumentadoComoPublico() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
               .andExpect(jsonPath("$.paths['/api/auth/login'].post.security").isEmpty());
    }

    /**
     * O requisito global de segurança vive na raiz do documento e vale para toda
     * operação que não o sobrescreva — por isso o login precisa negá-lo
     * explicitamente (ver loginDocumentadoComoPublico).
     */
    @Test
    void endpointsDeNegocioExigemTokenPorPadrao() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
               .andExpect(jsonPath("$.security[0]['bearer-jwt']").exists())
               .andExpect(jsonPath("$.paths['/api/solicitacoes'].get.security").doesNotExist());
    }

    @Test
    void endpointsEModelosDocumentados() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
               .andExpect(jsonPath("$.paths['/api/solicitacoes/{id}/status'].patch.summary").exists())
               .andExpect(jsonPath("$.paths['/api/solicitacoes/{id}/historico'].get.summary").exists())
               .andExpect(jsonPath("$.components.schemas.SolicitacaoResponse").exists())
               .andExpect(jsonPath("$.components.schemas.AlteracaoStatusRequest.properties.statusId.example")
                       .exists());
    }

    /**
     * Trava a cobertura: um endpoint novo sem @Operation quebra este teste em vez
     * de passar despercebido. As anotações não são verificadas por mais nada.
     */
    @Test
    void todaOperacaoTemSummary() throws Exception {
        String doc = mockMvc.perform(get("/v3/api-docs"))
                            .andReturn().getResponse().getContentAsString();
        JsonNode paths = new ObjectMapper().readTree(doc).get("paths");

        List<String> semSummary = new ArrayList<>();
        paths.properties().forEach(rota ->
            rota.getValue().properties().forEach(operacao -> {
                if (!operacao.getValue().hasNonNull("summary")) {
                    semSummary.add(operacao.getKey().toUpperCase() + " " + rota.getKey());
                }
            }));

        assertTrue(semSummary.isEmpty(), "operações sem summary: " + semSummary);
    }

    @Test
    void liberarADocumentacaoNaoAbriuOsEndpointsDeNegocio() throws Exception {
        mockMvc.perform(get("/api/alunos")).andExpect(status().isUnauthorized());
    }
}
