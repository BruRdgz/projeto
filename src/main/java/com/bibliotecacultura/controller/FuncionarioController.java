package com.bibliotecacultura.controller;

import com.bibliotecacultura.dto.FuncionarioDTO;
import com.bibliotecacultura.entity.Funcionario;
import com.bibliotecacultura.service.FuncionarioService;
import com.bibliotecacultura.session.SessaoFuncionario;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;

/**
 * Rotas /funcionarios — apenas BIBLIOTECARIO_ADM tem acesso.
 * O SessaoInterceptor já barra quem não tem cargo ADM antes de entrar aqui;
 * mas validamos novamente no POST por segurança.
 */
@Controller
@RequestMapping("/funcionarios")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    public FuncionarioController(FuncionarioService funcionarioService) {
        this.funcionarioService = funcionarioService;
    }

    /** Verifica sessão ADM e lança redirect se necessário */
    private boolean naoEAdm(HttpSession session) {
        SessaoFuncionario s = (SessaoFuncionario) session.getAttribute("sessao");
        return s == null || !s.isAdm();
    }

    // ── GET /funcionarios — lista / pesquisa ──────────────────────────────────
    @GetMapping
    public String listar(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("funcionarios", funcionarioService.listarTodos(q));
        model.addAttribute("q", q);
        return "consulta-funcionario";
    }

    // ── POST /funcionarios — salva funcionário ────────────────────────────────
    @PostMapping
    public String salvar(@Valid @ModelAttribute("funcionario") FuncionarioDTO dto,
                         BindingResult result,
                         HttpSession session,
                         Model model,
                         RedirectAttributes ra) {

        if (naoEAdm(session)) {
            ra.addFlashAttribute("erro", "Acesso negado.");
            return "redirect:/homescreen";
        }

        if (result.hasErrors()) {
            model.addAttribute("cargos", Arrays.asList(Funcionario.Cargo.values()));
            return "cadastro-funcionario";
        }

        try {
            funcionarioService.cadastrar(dto);
            ra.addFlashAttribute("sucesso", "Funcionário cadastrado com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Não foi possível concluir a operação.");
            return "redirect:/cadastro-funcionario";
        }

        return "redirect:/consulta-funcionario";
    }

    // ── GET /funcionarios/{id}/editar ─────────────────────────────────────────
    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, HttpSession session, Model model) {
        if (naoEAdm(session)) return "redirect:/homescreen?acesso=negado";

        model.addAttribute("funcionario", funcionarioService.buscarPorId(id));
        model.addAttribute("cargos", Arrays.asList(Funcionario.Cargo.values()));
        return "cadastro-funcionario";
    }

    // ── POST /funcionarios/{id} — atualiza funcionário ────────────────────────
    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("funcionario") FuncionarioDTO dto,
                            BindingResult result,
                            HttpSession session,
                            Model model,
                            RedirectAttributes ra) {

        if (naoEAdm(session)) {
            ra.addFlashAttribute("erro", "Acesso negado.");
            return "redirect:/homescreen";
        }

        if (result.hasErrors()) {
            model.addAttribute("cargos", Arrays.asList(Funcionario.Cargo.values()));
            return "cadastro-funcionario";
        }

        try {
            funcionarioService.alterar(id, dto);
            ra.addFlashAttribute("sucesso", "Funcionário atualizado com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Não foi possível concluir a operação.");
        }

        return "redirect:/consulta-funcionario";
    }

    // ── POST /funcionarios/{id}/deletar — exclui funcionário ─────────────────
    @PostMapping("/{id}/deletar")
    public String deletar(@PathVariable Long id,
                          HttpSession session,
                          RedirectAttributes ra) {
        if (naoEAdm(session)) {
            ra.addFlashAttribute("erro", "Não foi possível concluir a operação.");
            return "redirect:/homescreen";
        }

        try {
            funcionarioService.deletar(id);
            ra.addFlashAttribute("sucesso", "Funcionário excluído com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Não foi possível concluir a operação.");
        }

        return "redirect:/consulta-funcionario";
    }
}
