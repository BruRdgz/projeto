package com.bibliotecacultura.controller;

import java.util.List;
import java.util.Map;

import com.bibliotecacultura.dto.ClienteDTO;
import com.bibliotecacultura.dto.EmprestimoDTO;
import com.bibliotecacultura.entity.Exemplar;
import com.bibliotecacultura.repository.ExemplarRepository;
import com.bibliotecacultura.service.ClienteService;
import com.bibliotecacultura.service.CirculacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints JSON consumidos via fetch() na tela de realizar-emprestimo.html.
 * Não renderizam templates – retornam dados para o JS da página.
 */
@RestController
@RequestMapping("/api")
public class APIController {

    private final ClienteService clienteService;
    private final ExemplarRepository exemplarRepo;
    private final CirculacaoService circulacaoService;

    public APIController(ClienteService clienteService,
                         ExemplarRepository exemplarRepo,
                         CirculacaoService circulacaoService) {
        this.clienteService = clienteService;
        this.exemplarRepo   = exemplarRepo;
        this.circulacaoService = circulacaoService;
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
    @GetMapping("/clientes/cpf/{cpf}/emprestimos-ativos")
    public ResponseEntity<List<Map<String, Object>>> listarEmprestimosAtivos(@PathVariable String cpf) {
        try {
            List<Map<String, Object>> resp = circulacaoService.listarAtivosDoCliente(cpf).stream()
                    .map(this::toEmprestimoMap)
                    .toList();
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/exemplares/{id}")
    public ResponseEntity<Map<String, Object>> buscarExemplar(@PathVariable String id) {
        Long exemplarId = parseId(id);
        if (exemplarId == null) {
            return ResponseEntity.badRequest().build();
        }

        var exemplarOpt = exemplarRepo.findById(exemplarId);

        if (exemplarOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Exemplar e = exemplarOpt.get();

        if (e.getStatus() != Exemplar.Status.DISPONIVEL) {
            return ResponseEntity.<Map<String, Object>>badRequest().build();
        }

        Map<String, Object> resp = Map.of(
                "id",          e.getId(),
                "codigo",      "EX-" + e.getId(),
                "livroId",     e.getLivro().getId(),
                "livroCodigo", "LIV-" + e.getLivro().getId(),
                "livroTitulo", e.getLivro().getTitulo(),
                "livroAutor",  e.getLivro().getAutor(),
                "status",      e.getStatus().name()
        );

        return ResponseEntity.ok(resp);
    }

    @GetMapping("/emprestimos/exemplar/{id}")
    public ResponseEntity<Map<String, Object>> buscarEmprestimoAtivoPorExemplar(@PathVariable String id) {
        Long exemplarId = parseId(id);
        if (exemplarId == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            return ResponseEntity.ok(toEmprestimoMap(circulacaoService.buscarEmprestimoAtivoPorExemplar(exemplarId)));
        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
        }
    }

    private Map<String, Object> toEmprestimoMap(EmprestimoDTO e) {
        return Map.of(
                "emprestimoId", e.getId(),
                "exemplarId", e.getExemplarId(),
                "exemplarCodigo", "EX-" + e.getExemplarId(),
                "clienteCpf", e.getClienteCpf(),
                "clienteNome", e.getClienteNome(),
                "livroTitulo", e.getLivroTitulo(),
                "livroAutor", e.getLivroAutor(),
                "dataPrevisao", e.getDataPrevisao().toString()
        );
    }

    private Long parseId(String raw) {
        if (raw == null) return null;

        String digits = raw.replaceAll("\\D", "");
        if (digits.isBlank()) return null;

        try {
            return Long.valueOf(digits);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
