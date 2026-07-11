package br.com.samuel.documentos_academicos.mapper;

import org.springframework.stereotype.Component;

import br.com.samuel.documentos_academicos.dto.request.CursoRequest;
import br.com.samuel.documentos_academicos.dto.response.CursoResponse;
import br.com.samuel.documentos_academicos.entity.Curso;

@Component
public class CursoMapper {

    public Curso toEntity(CursoRequest req) {
        Curso curso = new Curso();
        curso.setNome(req.nome());
        return curso;
    }

    public CursoResponse toResponse(Curso curso) {
        return new CursoResponse(curso.getId(), curso.getNome());
    }
}