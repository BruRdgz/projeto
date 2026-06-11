package com.bibliotecacultura.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Gênero/categoria do livro — preenche o JComboBox no INT03.
 */
@Entity
@Table(name = "categorias")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String nome;

    @OneToMany(mappedBy = "categoria", fetch = FetchType.LAZY)
    private List<Livro> livros = new ArrayList<>();

    // ---- Getters e Setters ----

    public Long getId() { return id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public List<Livro> getLivros() { return livros; }

    @Override
    public String toString() { return nome; } // conveniente para renderização do JComboBox
}
