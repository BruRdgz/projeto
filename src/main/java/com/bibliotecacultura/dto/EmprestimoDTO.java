package com.bibliotecacultura.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDate;

public class EmprestimoDTO {

    private Long id;

    @NotBlank(message = "CPF do cliente é obrigatório")
    @Pattern(regexp = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", message = "CPF inválido")
    private String clienteCpf;

    @NotNull(message = "Livro é obrigatório")
    private Long livroId;

    // preenchido na leitura
    private Long exemplarId;
    private String clienteNome;
    private String livroTitulo;
    private String livroAutor;
    private LocalDate dataSaida;
    private LocalDate dataPrevisao;
    private LocalDate dataDevolucaoReal;
    private BigDecimal multaAplicada;
    private boolean ativo;

    // ---- Getters e Setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClienteCpf() { return clienteCpf; }
    public void setClienteCpf(String clienteCpf) { this.clienteCpf = clienteCpf; }

    public Long getLivroId() { return livroId; }
    public void setLivroId(Long livroId) { this.livroId = livroId; }

    public Long getExemplarId() { return exemplarId; }
    public void setExemplarId(Long exemplarId) { this.exemplarId = exemplarId; }

    public String getClienteNome() { return clienteNome; }
    public void setClienteNome(String clienteNome) { this.clienteNome = clienteNome; }

    public String getLivroTitulo() { return livroTitulo; }
    public void setLivroTitulo(String livroTitulo) { this.livroTitulo = livroTitulo; }

    public String getLivroAutor() { return livroAutor; }
    public void setLivroAutor(String livroAutor) { this.livroAutor = livroAutor; }

    public LocalDate getDataSaida() { return dataSaida; }
    public void setDataSaida(LocalDate dataSaida) { this.dataSaida = dataSaida; }

    public LocalDate getDataPrevisao() { return dataPrevisao; }
    public void setDataPrevisao(LocalDate dataPrevisao) { this.dataPrevisao = dataPrevisao; }

    public LocalDate getDataDevolucaoReal() { return dataDevolucaoReal; }
    public void setDataDevolucaoReal(LocalDate d) { this.dataDevolucaoReal = d; }

    public BigDecimal getMultaAplicada() { return multaAplicada; }
    public void setMultaAplicada(BigDecimal multaAplicada) { this.multaAplicada = multaAplicada; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
