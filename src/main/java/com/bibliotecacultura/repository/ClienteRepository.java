package com.bibliotecacultura.repository;

import com.bibliotecacultura.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // RF006 / RF009 — busca primária por CPF
    Optional<Cliente> findByCpfAndAtivoTrue(String cpf);

    // RF011 — pesquisar por CPF (exato) ou nome parcial
    @Query("""
            SELECT c FROM Cliente c
            WHERE c.ativo = true
              AND (c.cpf = :termo OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :termo, '%')))
            ORDER BY c.nome
            """)
    List<Cliente> search(@Param("termo") String termo);

    List<Cliente> findAllByAtivoTrueOrderByNomeAsc();

    boolean existsByCpf(String cpf);
}
