package br.com.samuel.documentos_academicos.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Setter;
import lombok.Getter;

@Entity
@Table(name = "aluno")
@Getter @Setter
public class Aluno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @OneToMany(mappedBy = "aluno", fetch = FetchType.LAZY)
    private List<Solicitacao> solicitacoes = new ArrayList<>();
}
