package br.com.samuel.documentos_academicos.mapper;

import org.springframework.stereotype.Component;

import br.com.samuel.documentos_academicos.dto.request.AlunoRequest;
import br.com.samuel.documentos_academicos.dto.response.AlunoResponse;
import br.com.samuel.documentos_academicos.entity.Aluno;

@Component
public class AlunoMapper {

    public Aluno toEntity(AlunoRequest req) {
        Aluno aluno = new Aluno();
        aluno.setNome(req.nome());
        aluno.setAtivo(true);
        return aluno;
    }

    public AlunoResponse toResponse(Aluno aluno) {
        return new AlunoResponse(aluno.getId(), aluno.getNome(), aluno.isAtivo());
    }
}