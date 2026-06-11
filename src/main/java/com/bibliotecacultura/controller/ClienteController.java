package com.bibliotecacultura.controller;

import com.bibliotecacultura.dto.ClienteDTO;
import com.bibliotecacultura.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    // ── GET /clientes — list / search (RF011) ─────────────────────────────────
    @GetMapping
    public String listar(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("clientes", clienteService.pesquisar(q));
        model.addAttribute("q", q);
        return "clientes/lista";
    }

    // ── GET /clientes/novo — blank form (RF010) ───────────────────────────────
    @GetMapping("/novo")
    public String novoForm(Model model) {
        model.addAttribute("cliente", new ClienteDTO());
        return "clientes/form";
    }

    // ── POST /clientes — save new client (RF010) ──────────────────────────────
    @PostMapping
    public String salvar(@Valid @ModelAttribute("cliente") ClienteDTO dto,
                         BindingResult result,
                         RedirectAttributes ra) {
        if (result.hasErrors()) return "clientes/form";
        clienteService.cadastrar(dto);
        ra.addFlashAttribute("sucesso", "Cliente cadastrado com sucesso!");
        return "redirect:/clientes";
    }

    // ── GET /clientes/{id}/editar — pre-filled form (RF012) ──────────────────
    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model) {
        model.addAttribute("cliente", clienteService.buscarPorId(id));
        return "clientes/form";
    }

    // ── POST /clientes/{id} — update client (RF012) ───────────────────────────
    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("cliente") ClienteDTO dto,
                            BindingResult result,
                            RedirectAttributes ra) {
        if (result.hasErrors()) return "clientes/form";
        clienteService.alterar(id, dto);
        ra.addFlashAttribute("sucesso", "Cliente atualizado com sucesso!");
        return "redirect:/clientes";
    }

    // ── POST /clientes/{id}/inativar — soft-delete (RF013) ───────────────────
    @PostMapping("/{id}/inativar")
    public String inativar(@PathVariable Long id, RedirectAttributes ra) {
        clienteService.inativar(id);
        ra.addFlashAttribute("sucesso", "Cliente inativado com sucesso!");
        return "redirect:/clientes";
    }
}
