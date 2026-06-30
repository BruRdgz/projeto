package com.bibliotecacultura.repository;

import com.bibliotecacultura.entity.Emprestimo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {

    // RF006 pré-condição: cliente deve ter menos de 3 empréstimos ativos
    long countByClienteIdAndDataDevolucaoRealIsNull(Long clienteId);

    // RF008: encontrar o empréstimo ativo para uma cópia específica (pelo ID do exemplar)
    @Query("""
            SELECT e FROM Emprestimo e
            JOIN FETCH e.cliente
            JOIN FETCH e.exemplar ex
            JOIN FETCH ex.livro
            WHERE ex.id = :exemplarId AND e.dataDevolucaoReal IS NULL
            """)
    Optional<Emprestimo> findActivoByExemplarId(@Param("exemplarId") Long exemplarId);

    // INT07 barra lateral: empréstimos ativos para um cliente
    @Query("""
            SELECT e FROM Emprestimo e
            JOIN FETCH e.exemplar ex
            JOIN FETCH ex.livro
            WHERE e.cliente.id = :clienteId AND e.dataDevolucaoReal IS NULL
            ORDER BY e.dataPrevisao
            """)
    List<Emprestimo> findActivosByClienteId(@Param("clienteId") Long clienteId);

    // INT08: histórico completo para um cliente (ativo + devolvido)
    @Query("""
            SELECT e FROM Emprestimo e
            JOIN FETCH e.exemplar ex
            JOIN FETCH ex.livro
            WHERE e.cliente.id = :clienteId
            ORDER BY e.dataSaida DESC
            """)
    List<Emprestimo> findAllByClienteId(@Param("clienteId") Long clienteId);

    // RF013 pré-condição: cliente só pode ser inativado se não tiver empréstimos ativos
    boolean existsByClienteIdAndDataDevolucaoRealIsNull(Long clienteId);

    boolean existsByFuncionarioId(Long funcionarioId);
}
