package com.bibliotecacultura.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Usuário da biblioteca (aluno ou membro da equipe emprestando livros).
 * RF010-RF013
 */
@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    @Column(nullable = false, length = 120)
    private String email;

    @Column(nullable = false, length = 20)
    private String telefone;

    /**
     * SEM_MULTA  — pode emprestar (padrão)
     * COM_MULTA  — devolveu com atraso; multado, mas ainda abaixo do limite
     * BLOQUEADO  — bloqueado de novos empréstimos até que a multa seja quitada
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status_situacional", nullable = false, length = 15)
    private StatusSituacional statusSituacional = StatusSituacional.SEM_MULTA;

    @Column(name = "saldo_multa", nullable = false, precision = 8, scale = 2)
    private BigDecimal saldoMulta = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    public enum StatusSituacional {
        SEM_MULTA, COM_MULTA, BLOQUEADO
    }

    // ---- Getters e Setters ----

    public Long getId() { return id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public StatusSituacional getStatusSituacional() { return statusSituacional; }
    public void setStatusSituacional(StatusSituacional status) { this.statusSituacional = status; }

    public BigDecimal getSaldoMulta() { return saldoMulta; }
    public void setSaldoMulta(BigDecimal saldoMulta) { this.saldoMulta = saldoMulta; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public LocalDateTime getCriadoEm() { return criadoEm; }

    /** Verificação de conveniência usada em toda a camada de serviço */
    public boolean podeEmprestar() {
        return ativo && statusSituacional == StatusSituacional.SEM_MULTA;
    }
}
