package com.bibliotecacultura.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Membro da equipe / operador do sistema.
 * RF014, RF015, RNF008
 *
 * Cargos:
 *  BIBLIOTECARIO      — acesso às operações do dia a dia (empréstimos, acervo, clientes)
 *  BIBLIOTECARIO_ADM  — tudo acima + pode cadastrar/editar funcionários
 */
@Entity
@Table(name = "funcionarios")
public class Funcionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    @Column(nullable = false, unique = true, length = 30)
    private String matricula;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Cargo cargo;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    public enum Cargo {
        BIBLIOTECARIO,
        BIBLIOTECARIO_ADM;

        /** Rótulo amigável para exibição nas telas */
        public String getLabel() {
            return switch (this) {
                case BIBLIOTECARIO     -> "Bibliotecário";
                case BIBLIOTECARIO_ADM -> "Bibliotecário ADM";
            };
        }

        /** Verifica se este cargo tem permissão de administração de funcionários */
        public boolean isAdm() {
            return this == BIBLIOTECARIO_ADM;
        }
    }

    // ---- Getters e Setters ----

    public Long getId() { return id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public Cargo getCargo() { return cargo; }
    public void setCargo(Cargo cargo) { this.cargo = cargo; }

    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
}