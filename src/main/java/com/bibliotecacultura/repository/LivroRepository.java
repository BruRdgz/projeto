package com.bibliotecacultura.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bibliotecacultura.entity.Livro;

public interface LivroRepository extends JpaRepository<Livro, Long> {

    // RF003 — pesquisar por título, autor ou nome da categoria
    @Query("""
            SELECT DISTINCT l FROM Livro l
            JOIN FETCH l.categoria c
            LEFT JOIN FETCH l.exemplares
            WHERE LOWER(l.titulo) LIKE LOWER(CONCAT('%', :termo, '%'))
               OR LOWER(l.autor)  LIKE LOWER(CONCAT('%', :termo, '%'))
               OR LOWER(c.nome)   LIKE LOWER(CONCAT('%', :termo, '%'))
            ORDER BY l.titulo
            """)
    List<Livro> search(@Param("termo") String termo);

    // Carregamento antecipado (Eager-load) para exibição em tabela (evita N+1)
    @Query("SELECT DISTINCT l FROM Livro l JOIN FETCH l.categoria LEFT JOIN FETCH l.exemplares ORDER BY l.titulo")
    List<Livro> findAllWithDetails();

    // Carregamento antecipado (Eager-load) de um único livro
    @Query("SELECT l FROM Livro l JOIN FETCH l.categoria LEFT JOIN FETCH l.exemplares WHERE l.id = :id")
    Optional<Livro> findByIdWithDetails(@Param("id") Long id);


    @Query("""
        SELECT DISTINCT l FROM Livro l
        JOIN FETCH l.categoria c
        LEFT JOIN FETCH l.exemplares
        WHERE LOWER(l.titulo) LIKE LOWER(CONCAT('%', :termo, '%'))
        ORDER BY l.titulo
        """)
List<Livro> searchByTitulo(@Param("termo") String termo);

@Query("""
        SELECT DISTINCT l FROM Livro l
        JOIN FETCH l.categoria c
        LEFT JOIN FETCH l.exemplares
        WHERE LOWER(l.autor) LIKE LOWER(CONCAT('%', :termo, '%'))
        ORDER BY l.titulo
        """)
List<Livro> searchByAutor(@Param("termo") String termo);

@Query("""
        SELECT DISTINCT l FROM Livro l
        JOIN FETCH l.categoria c
        LEFT JOIN FETCH l.exemplares
        WHERE LOWER(c.nome) LIKE LOWER(CONCAT('%', :termo, '%'))
        ORDER BY l.titulo
        """)
List<Livro> searchByGenero(@Param("termo") String termo);
}
