package com.bibliotecacultura.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Registro de livro no catálogo da biblioteca.
 * RF002-RF005
 */
@Entity
@Table(name = "livros")
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, length = 150)
    private String autor;

    @Column(name = "ano_publicacao", nullable = false)
    private int anoPublicacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    /**
     * Um exemplar é criado automaticamente quando o livro é registrado (RF002 passo 16).
     * CascadeType.ALL + orphanRemoval lida com a exclusão em cascata (RF005).
     */
    @OneToMany(mappedBy = "livro", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Exemplar> exemplares = new ArrayList<>();

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    // ---- Getters e Setters ----

    public Long getId() { return id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public int getAnoPublicacao() { return anoPublicacao; }
    public void setAnoPublicacao(int anoPublicacao) { this.anoPublicacao = anoPublicacao; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public List<Exemplar> getExemplares() { return exemplares; }

    public LocalDateTime getCriadoEm() { return criadoEm; }

    /**
     * Conveniência usada pelo INT04: retorna "DISPONIVEL" se pelo menos um
     * exemplar estiver disponível, caso contrário "INDISPONIVEL".
     */
    public String getStatusGeral() {
        return exemplares.stream()
                .anyMatch(e -> e.getStatus() == Exemplar.Status.DISPONIVEL)
                ? "DISPONIVEL"
                : "INDISPONIVEL";
    }
}
