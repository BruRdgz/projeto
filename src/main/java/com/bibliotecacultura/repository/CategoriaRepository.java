package com.bibliotecacultura.repository;

import com.bibliotecacultura.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findAllByOrderByNomeAsc();

    boolean existsByNomeIgnoreCase(String nome);
}
