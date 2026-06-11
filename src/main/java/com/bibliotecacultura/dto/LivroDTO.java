package com.bibliotecacultura.dto;

import jakarta.validation.constraints.*;

public class LivroDTO {

    private Long id;

    @NotBlank(message = "Título é obrigatório")
    @Size(max = 255)
    private String titulo;

    @NotBlank(message = "Autor é obrigatório")
    @Size(max = 150)
    private String autor;

    @NotNull(message = "Ano de publicação é obrigatório")
    @Min(value = 1000, message = "Ano inválido")
    @Max(value = 9999, message = "Ano inválido")
    private Integer anoPublicacao;

    @NotNull(message = "Categoria é obrigatória")
    private Long categoriaId;

    // preenchido na leitura, ignorado na escrita
    private String categoriaNome;
    private String statusGeral;

    // ---- Getters e Setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public Integer getAnoPublicacao() { return anoPublicacao; }
    public void setAnoPublicacao(Integer anoPublicacao) { this.anoPublicacao = anoPublicacao; }

    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }

    public String getCategoriaNome() { return categoriaNome; }
    public void setCategoriaNome(String categoriaNome) { this.categoriaNome = categoriaNome; }

    public String getStatusGeral() { return statusGeral; }
    public void setStatusGeral(String statusGeral) { this.statusGeral = statusGeral; }
}
