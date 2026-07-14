package br.com.samuel.documentos_academicos.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.samuel.documentos_academicos.dto.request.AlunoRequest;
import jakarta.validation.Valid;

class GlobalExceptionHandlerTest {

    private final MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new TestController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void recursoNaoEncontradoRetorna404NoFormatoPadrao() throws Exception {
        mvc.perform(get("/test/nao-encontrado"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.erro").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.mensagem").value("Aluno 99 não encontrado"))
                .andExpect(jsonPath("$.path").value("/test/nao-encontrado"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.campos").isArray());
    }

    @Test
    void recursoDuplicadoRetorna409() throws Exception {
        mvc.perform(get("/test/duplicado"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.erro").value("Recurso duplicado"));
    }

    @Test
    void regraNegocioRetorna422() throws Exception {
        mvc.perform(get("/test/regra"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.erro").value("Regra de negócio inválida"));
    }

    @Test
    void validacaoRetorna400ComCamposInvalidos() throws Exception {
        mvc.perform(post("/test/validacao")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.erro").value("Erro de validação"))
                .andExpect(jsonPath("$.campos").isNotEmpty())
                .andExpect(jsonPath("$.campos[0].campo").value("nome"));
    }

    @Test
    void jsonMalformadoRetorna400() throws Exception {
        mvc.perform(post("/test/validacao")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ isto nao e json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Requisição inválida"));
    }

    @Test
    void erroInesperadoRetorna500SemExporStackTrace() throws Exception {
        mvc.perform(get("/test/erro"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.erro").value("Erro interno"))
                .andExpect(jsonPath("$.mensagem", Matchers.containsString("Código de referência")))
                // nao deve vazar stack trace nem detalhes internos
                .andExpect(content().string(Matchers.not(Matchers.containsString("Exception"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("at br.com.samuel"))));
    }

    @RestController
    @RequestMapping("/test")
    static class TestController {

        @org.springframework.web.bind.annotation.GetMapping("/nao-encontrado")
        void naoEncontrado() {
            throw new RecursoNaoEncontradoException("Aluno 99 não encontrado");
        }

        @org.springframework.web.bind.annotation.GetMapping("/duplicado")
        void duplicado() {
            throw new RecursoDuplicadoException("Curso já cadastrado");
        }

        @org.springframework.web.bind.annotation.GetMapping("/regra")
        void regra() {
            throw new RegraNegocioException("Transição de status inválida");
        }

        @org.springframework.web.bind.annotation.GetMapping("/erro")
        void erro() {
            throw new IllegalStateException("falha interna qualquer");
        }

        @PostMapping("/validacao")
        void validacao(@Valid @RequestBody AlunoRequest req) {
            // corpo vazio de proposito: so exercita a validacao
        }
    }
}