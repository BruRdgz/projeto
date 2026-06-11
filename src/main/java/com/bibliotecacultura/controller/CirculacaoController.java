package com.bibliotecacultura.controller;

import com.bibliotecacultura.service.CirculacaoService;
import com.bibliotecacultura.service.AcervoService;
import com.bibliotecacultura.service.ClienteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/circulacao")
public class CirculacaoController {

    // TODO: replace hardcoded ID with session-based logged-in user (deferred auth task)
    private static final Long FUNCIONARIO_PLACEHOLDER_ID = 1L;

    private final CirculacaoService circulacaoService;
    private final AcervoService acervoService;
    private final ClienteService clienteService;

    public CirculacaoController(CirculacaoService circulacaoService,
                                AcervoService acervoService,
                                ClienteService clienteService) {
        this.circulacaoService = circulacaoService;
        this.acervoService     = acervoService;
        this.clienteService    = clienteService;
    }

    // ── GET /circulacao/emprestimo — loan form (RF006) ────────────────────────
    @GetMapping("/emprestimo")
    public String emprestimoForm(Model model) {
        model.addAttribute("livros", acervoService.listarTodos());
        return "circulacao/emprestimo";
    }

    // ── POST /circulacao/emprestimo — register loan (RF006) ───────────────────
    @PostMapping("/emprestimo")
    public String registrarEmprestimo(@RequestParam String clienteCpf,
                                      @RequestParam Long livroId,
                                      RedirectAttributes ra) {
        circulacaoService.registrarEmprestimo(clienteCpf, livroId, FUNCIONARIO_PLACEHOLDER_ID);
        ra.addFlashAttribute("sucesso", "Empréstimo registrado com sucesso!");
        return "redirect:/circulacao/emprestimo";
    }

    // ── GET /circulacao/devolucao — return form (RF008) ───────────────────────
    @GetMapping("/devolucao")
    public String devolucaoForm() {
        return "circulacao/devolucao";
    }

    // ── POST /circulacao/devolucao — register return (RF008) ──────────────────
    @PostMapping("/devolucao")
    public String registrarDevolucao(@RequestParam Long exemplarId,
                                     RedirectAttributes ra) {
        var dto = circulacaoService.registrarDevolucao(exemplarId);
        if (dto.getMultaAplicada() != null &&
                dto.getMultaAplicada().compareTo(java.math.BigDecimal.ZERO) > 0) {
            ra.addFlashAttribute("aviso",
                    "Devolução registrada com multa de R$ " + dto.getMultaAplicada() + ".");
        } else {
            ra.addFlashAttribute("sucesso", "Devolução registrada com sucesso!");
        }
        return "redirect:/circulacao/devolucao";
    }

    // ── GET /circulacao/renovacao — renewal form (RF007) ─────────────────────
    @GetMapping("/renovacao")
    public String renovacaoForm(@RequestParam(required = false) String cpf, Model model) {
        if (cpf != null && !cpf.isBlank()) {
            model.addAttribute("emprestimos", circulacaoService.listarAtivosDoCliente(cpf));
            model.addAttribute("cpf", cpf);
        }
        return "circulacao/renovacao";
    }

    // ── POST /circulacao/renovacao/{id} — renew loan (RF007) ─────────────────
    @PostMapping("/renovacao/{emprestimoId}")
    public String renovar(@PathVariable Long emprestimoId,
                          @RequestParam String cpf,
                          RedirectAttributes ra) {
        circulacaoService.renovarEmprestimo(emprestimoId);
        ra.addFlashAttribute("sucesso", "Empréstimo renovado com sucesso!");
        return "redirect:/circulacao/renovacao?cpf=" + cpf;
    }

    // ── GET /circulacao/multas — fine management (RF009) ─────────────────────
    @GetMapping("/multas")
    public String multasForm(@RequestParam(required = false) String cpf, Model model) {
        if (cpf != null && !cpf.isBlank()) {
            model.addAttribute("cliente", clienteService.buscarPorCpf(cpf));
            model.addAttribute("historico", circulacaoService.listarHistoricoDoCliente(cpf));
            model.addAttribute("cpf", cpf);
        }
        return "circulacao/multas";
    }

    // ── POST /circulacao/multas/quitar — clear fine (RF009) ──────────────────
    @PostMapping("/multas/quitar")
    public String quitarMulta(@RequestParam String cpf, RedirectAttributes ra) {
        circulacaoService.quitarMulta(cpf);
        ra.addFlashAttribute("sucesso", "Multa quitada. Cliente liberado para novos empréstimos.");
        return "redirect:/circulacao/multas?cpf=" + cpf;
    }
}
