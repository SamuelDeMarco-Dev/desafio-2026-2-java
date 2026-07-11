package br.com.samuel.documentos_academicos.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import br.com.samuel.documentos_academicos.dto.request.AlunoRequest;
import br.com.samuel.documentos_academicos.dto.response.AlunoResponse;
import br.com.samuel.documentos_academicos.entity.Aluno;

class AlunoMapperTest {

    private final AlunoMapper mapper = new AlunoMapper();

    @Test
    void mapeiaEntidadeParaResponse() {
        Aluno aluno = new Aluno();
        aluno.setId(1L);
        aluno.setNome("Samuel");
        aluno.setAtivo(true);

        AlunoResponse response = mapper.toResponse(aluno);

        assertEquals(1L, response.id());
        assertEquals("Samuel", response.nome());
        assertTrue(response.ativo());
    }

    @Test
    void mapeiaRequestParaEntidadeComoAtivo() {
        AlunoRequest request = new AlunoRequest("Maria");

        Aluno aluno = mapper.toEntity(request);

        assertEquals("Maria", aluno.getNome());
        assertTrue(aluno.isAtivo(), "aluno criado deve iniciar ativo");
    }
}