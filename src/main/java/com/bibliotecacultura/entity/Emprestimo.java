package com.bibliotecacultura.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Registro de empréstimo vinculando um Cliente a um Exemplar.
 * RF006 — registrar empréstimo      (+14 dias de previsão)
 * RF007 — renovar empréstimo         (+7 dias a partir da data de vencimento atual)
 * RF008 — registrar devolução    (define dataDevolucaoReal, calcula multa)
 * RF009 — quitar multa         (tratado no serviço; sem necessidade de campo extra)
 */
@Entity
@Table(name = "emprestimos")
public class Emprestimo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exemplar_id", nullable = false)
    private Exemplar exemplar;

    @Column(name = "data_saida", nullable = false)
    private LocalDate dataSaida = LocalDate.now();

    /** Inicialmente dataSaida + 14 dias; estendido em +7 a cada renovação (RF007). */
    @Column(name = "data_previsao", nullable = false)
    private LocalDate dataPrevisao;

    /** NULL enquanto o empréstimo estiver ativo. Definido quando o livro é devolvido. */
    @Column(name = "data_devolucao_real")
    private LocalDate dataDevolucaoReal;

    @Column(name = "multa_aplicada", nullable = false, precision = 8, scale = 2)
    private BigDecimal multaAplicada = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private Funcionario funcionario;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    // ---- Getters e Setters ----

    public Long getId() { return id; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Exemplar getExemplar() { return exemplar; }
    public void setExemplar(Exemplar exemplar) { this.exemplar = exemplar; }

    public LocalDate getDataSaida() { return dataSaida; }
    public void setDataSaida(LocalDate dataSaida) { this.dataSaida = dataSaida; }

    public LocalDate getDataPrevisao() { return dataPrevisao; }
    public void setDataPrevisao(LocalDate dataPrevisao) { this.dataPrevisao = dataPrevisao; }

    public LocalDate getDataDevolucaoReal() { return dataDevolucaoReal; }
    public void setDataDevolucaoReal(LocalDate data) { this.dataDevolucaoReal = data; }

    public BigDecimal getMultaAplicada() { return multaAplicada; }
    public void setMultaAplicada(BigDecimal multa) { this.multaAplicada = multa; }

    public Funcionario getFuncionario() { return funcionario; }
    public void setFuncionario(Funcionario funcionario) { this.funcionario = funcionario; }

    public LocalDateTime getCriadoEm() { return criadoEm; }

    /** O empréstimo está ativo enquanto nenhuma data de devolução tiver sido definida. */
    public boolean isAtivo() { return dataDevolucaoReal == null; }
}
