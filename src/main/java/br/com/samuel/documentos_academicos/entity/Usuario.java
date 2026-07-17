package br.com.samuel.documentos_academicos.entity;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import br.com.samuel.documentos_academicos.enums.Perfil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "usuario")
@Audited
@Getter @Setter
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "login", nullable = false, length = 50, unique = true)
    private String login;

    /** Sempre o hash BCrypt — nunca a senha em texto puro. */
    @NotAudited
    @Column(name = "senha", nullable = false, length = 100)
    private String senha;

    @Column(name = "codigo_responsavel", unique = true)
    private Integer codigoResponsavel;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_perfil", joinColumns = @JoinColumn(name = "usuario_id"))
    @Column(name = "perfil", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Set<Perfil> perfis = new HashSet<>();
}