package com.bibliotecacultura.controller;

import com.bibliotecacultura.dto.ClienteDTO;
import com.bibliotecacultura.entity.Exemplar;
import com.bibliotecacultura.repository.ExemplarRepository;
import com.bibliotecacultura.service.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Endpoints JSON consumidos via fetch() na tela de realizar-emprestimo.html.
 * Não renderizam templates — retornam dados para o JS da página.
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

    /** GET /api/clientes/cpf/{cpf} — busca cliente pelo CPF para o card de empréstimo */
    @GetMapping("/clientes/cpf/{cpf}")
    public ResponseEntity<ClienteDTO> buscarClientePorCpf(@PathVariable String cpf) {
        try {
            return ResponseEntity.ok(clienteService.buscarPorCpf(cpf));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** GET /api/exemplares/{id} — busca exemplar pelo ID para o card de livro */
    @GetMapping("/exemplares/{id}")
    public ResponseEntity<Map<String, Object>> buscarExemplar(@PathVariable Long id) {
        return exemplarRepo.findById(id)
                .map(e -> {
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
                })
                .orElse(ResponseEntity.notFound().build());
    }
}