package com.bibliotecacultura.entity;

import jakarta.persistence.*;

/**
 * Cópia física de um livro.
 * Criado automaticamente quando um Livro é registrado (RF002 passo 16).
 * O status muda para INDISPONIVEL no empréstimo (RF006) e volta na devolução (RF008).
 */
@Entity
@Table(name = "exemplares")
public class Exemplar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "livro_id", nullable = false)
    private Livro livro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Status status = Status.DISPONIVEL;

    public enum Status {
        DISPONIVEL, INDISPONIVEL
    }

    // ---- Getters e Setters ----

    public Long getId() { return id; }

    public Livro getLivro() { return livro; }
    public void setLivro(Livro livro) { this.livro = livro; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
