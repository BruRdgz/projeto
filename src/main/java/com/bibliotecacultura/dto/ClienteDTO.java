package com.bibliotecacultura.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public class ClienteDTO {

    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 150)
    private String nome;

    @NotBlank(message = "CPF é obrigatório")
    @Pattern(regexp = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", message = "CPF inválido (use ###.###.###-##)")
    private String cpf;

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    @Size(max = 120)
    private String email;

    @NotBlank(message = "Telefone é obrigatório")
    @Size(max = 20)
    private String telefone;

    // campos somente leitura — nunca enviados pelo formulário
    private String statusSituacional;
    private BigDecimal saldoMulta;
    private List<String> livrosAtivos;

    // ---- Getters e Setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getStatusSituacional() { return statusSituacional; }
    public void setStatusSituacional(String s) { this.statusSituacional = s; }

    public BigDecimal getSaldoMulta() { return saldoMulta; }
    public void setSaldoMulta(BigDecimal saldoMulta) { this.saldoMulta = saldoMulta; }

    public List<String> getLivrosAtivos() { return livrosAtivos; }
    public void setLivrosAtivos(List<String> livrosAtivos) { this.livrosAtivos = livrosAtivos; }
}
