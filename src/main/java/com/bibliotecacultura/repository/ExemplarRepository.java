package com.bibliotecacultura.repository;

import com.bibliotecacultura.entity.Exemplar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ExemplarRepository extends JpaRepository<Exemplar, Long> {

    // RF005 pré-condição: só é possível excluir um livro se nenhuma de suas cópias estiver emprestada
    boolean existsByLivroIdAndStatus(Long livroId, Exemplar.Status status);

    // RF006: encontrar uma cópia disponível para empréstimo
    @Query("""
            SELECT e FROM Exemplar e
            WHERE e.livro.id = :livroId AND e.status = 'DISPONIVEL'
            ORDER BY e.id
            """)
    Optional<Exemplar> findFirstAvailableByLivro(@Param("livroId") Long livroId);
}
