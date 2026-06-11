package com.bibliotecacultura.controller;

import com.bibliotecacultura.dto.FuncionarioDTO;
import com.bibliotecacultura.entity.Funcionario;
import com.bibliotecacultura.service.FuncionarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;

@Controller
@RequestMapping("/funcionarios")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    public FuncionarioController(FuncionarioService funcionarioService) {
        this.funcionarioService = funcionarioService;
    }

    // ── GET /funcionarios — list / search (RF015) ─────────────────────────────
    @GetMapping
    public String listar(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("funcionarios", funcionarioService.listarTodos(q));
        model.addAttribute("q", q);
        return "funcionarios/lista";
    }

    // ── GET /funcionarios/novo — blank form (RF014) ───────────────────────────
    @GetMapping("/novo")
    public String novoForm(Model model) {
        model.addAttribute("funcionario", new FuncionarioDTO());
        model.addAttribute("cargos", Arrays.asList(Funcionario.Cargo.values()));
        return "funcionarios/form";
    }

    // ── POST /funcionarios — save new staff (RF014) ───────────────────────────
    @PostMapping
    public String salvar(@Valid @ModelAttribute("funcionario") FuncionarioDTO dto,
                         BindingResult result,
                         Model model,
                         RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("cargos", Arrays.asList(Funcionario.Cargo.values()));
            return "funcionarios/form";
        }
        funcionarioService.cadastrar(dto);
        ra.addFlashAttribute("sucesso", "Funcionário cadastrado com sucesso!");
        return "redirect:/funcionarios";
    }

    // ── GET /funcionarios/{id}/editar — pre-filled form (RF015) ──────────────
    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model) {
        model.addAttribute("funcionario", funcionarioService.buscarPorId(id));
        model.addAttribute("cargos", Arrays.asList(Funcionario.Cargo.values()));
        return "funcionarios/form";
    }

    // ── POST /funcionarios/{id} — update staff (RF015) ────────────────────────
    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("funcionario") FuncionarioDTO dto,
                            BindingResult result,
                            Model model,
                            RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("cargos", Arrays.asList(Funcionario.Cargo.values()));
            return "funcionarios/form";
        }
        funcionarioService.alterar(id, dto);
        ra.addFlashAttribute("sucesso", "Funcionário atualizado com sucesso!");
        return "redirect:/funcionarios";
    }
}
