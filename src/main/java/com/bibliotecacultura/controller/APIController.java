package com.bibliotecacultura.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bibliotecacultura.dto.ClienteDTO;
import com.bibliotecacultura.entity.Exemplar;
import com.bibliotecacultura.repository.ExemplarRepository;
import com.bibliotecacultura.service.ClienteService;

/**
 * Endpoints JSON consumidos via fetch() na tela de realizar-emprestimo.html.
 * Não renderizam templates – retornam dados para o JS da página.
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    private final ClienteService clienteService;
    private final ExemplarRepository exemplarRepo;

    public ApiController(ClienteService clienteService, ExemplarRepository exemplarRepo) {
        this.clienteService = clienteService;
        this.exemplarRepo   = exemplarRepo;
    }

    /** GET /api/clientes/cpf/{cpf} – busca cliente pelo CPF para o card de empréstimo */
    @GetMapping("/clientes/cpf/{cpf}")
    public ResponseEntity<ClienteDTO> buscarClientePorCpf(@PathVariable String cpf) {
        try {
            return ResponseEntity.ok(clienteService.buscarPorCpf(cpf));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** GET /api/exemplares/{id} – busca exemplar pelo ID para o card de livro */
    @GetMapping("/exemplares/{id}")
    public ResponseEntity<Map<String, Object>> buscarExemplar(@PathVariable Long id) {
        var exemplarOpt = exemplarRepo.findById(id);

        if (exemplarOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Exemplar e = exemplarOpt.get();

        if (e.getStatus() != Exemplar.Status.DISPONIVEL) {
            return ResponseEntity.<Map<String, Object>>badRequest().build();
        }

        Map<String, Object> resp = Map.of(
                "id",          e.getId(),
                "livroId",     e.getLivro().getId(),
                "livroTitulo", e.getLivro().getTitulo(),
                "livroAutor",  e.getLivro().getAutor(),
                "status",      e.getStatus().name()
        );

        return ResponseEntity.ok(resp);
    }
}