package com.bibliotecacultura.dto;

import jakarta.validation.constraints.*;

public class FuncionarioDTO {

    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 150)
    private String nome;

    @NotBlank(message = "CPF é obrigatório")
    @Pattern(regexp = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", message = "CPF inválido (use ###.###.###-##)")
    private String cpf;

    @NotBlank(message = "Matrícula é obrigatória")
    @Size(max = 30)
    private String matricula;

    /** "BIBLIOTECARIO" | "BIBLIOTECARIO_ADM" */
    @NotBlank(message = "Cargo é obrigatório")
    private String cargo;

    /** Obrigatório somente no cadastro; deixe em branco para manter a senha atual */
    @Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres")
    private String senha;

    private boolean ativo = true;

    // ---- Getters e Setters ----

    public Long getId()                    { return id; }
    public void setId(Long id)             { this.id = id; }

    public String getNome()                { return nome; }
    public void setNome(String nome)       { this.nome = nome; }

    public String getCpf()                 { return cpf; }
    public void setCpf(String cpf)         { this.cpf = cpf; }

    public String getMatricula()               { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getCargo()               { return cargo; }
    public void setCargo(String cargo)     { this.cargo = cargo; }

    public String getSenha()               { return senha; }
    public void setSenha(String senha)     { this.senha = senha; }

    public boolean isAtivo()               { return ativo; }
    public void setAtivo(boolean ativo)    { this.ativo = ativo; }
}