package br.com.samuel.documentos_academicos.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "status_solicitacao")
@Getter @Setter
public class Status {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, length = 30, unique = true)
    private String codigo;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "responsavel")
    private Integer responsavel;

    @Column(name = "finaliza_solicitacao", nullable = false)
    private boolean finalizaSolicitacao = false;
}
