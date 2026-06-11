package com.bibliotecacultura.repository;

import com.bibliotecacultura.entity.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

    // RF001 — busca de login
    Optional<Funcionario> findByMatriculaAndAtivoTrue(String matricula);

    // RF015 — listar / pesquisar
    @Query("""
            SELECT f FROM Funcionario f
            WHERE (:termo IS NULL OR :termo = ''
                   OR LOWER(f.nome)     LIKE LOWER(CONCAT('%', :termo, '%'))
                   OR f.cpf             LIKE CONCAT('%', :termo, '%')
                   OR CAST(f.cargo AS string) LIKE UPPER(CONCAT('%', :termo, '%')))
            ORDER BY f.nome
            """)
    List<Funcionario> search(@Param("termo") String termo);

    boolean existsByCpf(String cpf);
    boolean existsByMatricula(String matricula);
}
