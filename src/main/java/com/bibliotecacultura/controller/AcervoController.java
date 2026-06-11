package com.bibliotecacultura.controller;

import com.bibliotecacultura.dto.LivroDTO;
import com.bibliotecacultura.service.AcervoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/acervo")
public class AcervoController {

    private final AcervoService acervoService;

    public AcervoController(AcervoService acervoService) {
        this.acervoService = acervoService;
    }

    // ── GET /acervo — list / search (RF003) ───────────────────────────────────
    @GetMapping
    public String listar(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("livros", acervoService.pesquisar(q));
        model.addAttribute("q", q);
        return "acervo/lista";
    }

    // ── GET /acervo/novo — blank form (RF002) ─────────────────────────────────
    @GetMapping("/novo")
    public String novoForm(Model model) {
        model.addAttribute("livro", new LivroDTO());
        model.addAttribute("categorias", acervoService.listarCategorias());
        return "acervo/form";
    }

    // ── POST /acervo — save new book (RF002) ──────────────────────────────────
    @PostMapping
    public String salvar(@Valid @ModelAttribute("livro") LivroDTO dto,
                         BindingResult result,
                         Model model,
                         RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("categorias", acervoService.listarCategorias());
            return "acervo/form";
        }
        acervoService.cadastrar(dto);
        ra.addFlashAttribute("sucesso", "Livro cadastrado com sucesso!");
        return "redirect:/acervo";
    }

    // ── GET /acervo/{id}/editar — pre-filled form (RF004) ────────────────────
    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model) {
        model.addAttribute("livro", acervoService.buscarPorId(id));
        model.addAttribute("categorias", acervoService.listarCategorias());
        return "acervo/form";
    }

    // ── POST /acervo/{id} — update book (RF004) ───────────────────────────────
    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("livro") LivroDTO dto,
                            BindingResult result,
                            Model model,
                            RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("categorias", acervoService.listarCategorias());
            return "acervo/form";
        }
        acervoService.alterar(id, dto);
        ra.addFlashAttribute("sucesso", "Livro atualizado com sucesso!");
        return "redirect:/acervo";
    }

    // ── POST /acervo/{id}/deletar — delete book (RF005) ──────────────────────
    @PostMapping("/{id}/deletar")
    public String deletar(@PathVariable Long id, RedirectAttributes ra) {
        acervoService.deletar(id);
        ra.addFlashAttribute("sucesso", "Livro excluído com sucesso!");
        return "redirect:/acervo";
    }
}
