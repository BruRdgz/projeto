package com.bibliotecacultura.controller;

import com.bibliotecacultura.entity.Funcionario;
import com.bibliotecacultura.entity.Livro;
import com.bibliotecacultura.exception.NegocioException;
import com.bibliotecacultura.repository.LivroRepository;
import com.bibliotecacultura.service.ClienteService;
import com.bibliotecacultura.service.FuncionarioService;
import com.bibliotecacultura.session.SessaoFuncionario;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class ViewController {

    @Autowired private FuncionarioService funcionarioService;
    @Autowired private ClienteService clienteService;
    @Autowired private com.bibliotecacultura.service.CirculacaoService circulacaoService;
    @Autowired private LivroRepository livroRepository;

    // ── GET /login ─────────────────────────────────────────────────────────────
    @GetMapping("/login")
    public String paginaLogin(HttpSession session) {
        if (session.getAttribute("sessao") != null) {
            return "redirect:/homescreen";
        }
        return "login";
    }

    // ── POST /login ─────────────────────────────────────────────────────────────
    @PostMapping("/login")
    public String efetuarLogin(@RequestParam String matricula,
                               @RequestParam String senha,
                               HttpSession session,
                               RedirectAttributes ra) {
        try {
            SessaoFuncionario sessao = funcionarioService.autenticar(matricula, senha);
            session.setAttribute("sessao", sessao);
            return "redirect:/homescreen";
        } catch (NegocioException e) {
            ra.addFlashAttribute("erro", e.getMessage());
            return "redirect:/login";
        }
    }

    // ── GET /logout ─────────────────────────────────────────────────────────────
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // ── GET /homescreen ─────────────────────────────────────────────────────────
    @GetMapping("/homescreen")
    public String homeScreen(@RequestParam(required = false) String acesso, Model model) {
        if ("negado".equals(acesso)) {
            model.addAttribute("aviso", "Você não tem permissão para acessar essa área.");
        }
        return "homescreen";
    }

    // ── GET /esqueci-senha ──────────────────────────────────────────────────────
    @GetMapping("/esqueci-senha")
    public String paginaEsqueciSenha() {
        return "esqueci-senha";
    }

    // ── GET /redefinir-senha ────────────────────────────────────────────────────
    @GetMapping("/redefinir-senha")
    public String redefinirSenha() {
        return "redefinir-senha";
    }

    // ── GET /visualizar-acervo ──────────────────────────────────────────────────
    @GetMapping("/visualizar-acervo")
    public String paginaVisualizarAcervo(@RequestParam(defaultValue = "") String termo,
                                         @RequestParam(defaultValue = "titulo") String filtro,
                                         Model model) {
        List<Livro> livros = switch (filtro) {
            case "autor"  -> livroRepository.searchByAutor(termo);
            case "genero" -> livroRepository.searchByGenero(termo);
            default       -> livroRepository.searchByTitulo(termo);
        };
        model.addAttribute("livros", livros);
        model.addAttribute("termo", termo);
        model.addAttribute("filtro", filtro);
        return "visualizar-acervo";
    }

    // ── GET /realizar-emprestimo ─────────────────────────────────────────────────
    @GetMapping("/realizar-emprestimo")
    public String realizarEmprestimo() {
        return "realizar-emprestimo";
    }

    // ── GET /consulta-funcionario ────────────────────────────────────────────────
    @GetMapping("/consulta-funcionario")
    public String consultaFuncionario(@RequestParam(defaultValue = "") String q, Model model) {
        model.addAttribute("funcionarios", funcionarioService.listarTodos(q));
        model.addAttribute("q", q);
        return "consulta-funcionario";
    }

    // ── GET /cadastro-funcionario ────────────────────────────────────────────────
    @GetMapping("/cadastro-funcionario")
    public String cadastroFuncionario(Model model) {
        model.addAttribute("cargos", Funcionario.Cargo.values());
        return "cadastro-funcionario";
    }

    // ── POST /cadastro-funcionario ───────────────────────────────────────────────
    @PostMapping("/cadastro-funcionario")
    public String salvarFuncionario(@RequestParam String nome,
                                    @RequestParam String cpf,
                                    @RequestParam String matricula,
                                    @RequestParam String cargo,
                                    @RequestParam String senha,
                                    HttpSession session,
                                    RedirectAttributes ra) {
        SessaoFuncionario sessao = (SessaoFuncionario) session.getAttribute("sessao");
        if (sessao == null || !sessao.isAdm()) {
            ra.addFlashAttribute("erro", "Apenas bibliotecários ADM podem cadastrar funcionários.");
            return "redirect:/homescreen";
        }
        try {
            com.bibliotecacultura.dto.FuncionarioDTO dto = new com.bibliotecacultura.dto.FuncionarioDTO();
            dto.setNome(nome);
            dto.setCpf(cpf);
            dto.setMatricula(matricula);
            dto.setCargo(cargo);
            dto.setSenha(senha);
            funcionarioService.cadastrar(dto);
            ra.addFlashAttribute("sucesso", "Funcionário cadastrado com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/consulta-funcionario";
    }

    // ── GET /consulta-livro ──────────────────────────────────────────────────────
    @GetMapping("/consulta-livro")
    public String consultaLivro(@RequestParam(defaultValue = "") String termo,
                                @RequestParam(defaultValue = "titulo") String filtro,
                                Model model) {
        List<Livro> resultados = switch (filtro) {
            case "autor"  -> livroRepository.searchByAutor(termo);
            case "genero" -> livroRepository.searchByGenero(termo);
            default       -> livroRepository.searchByTitulo(termo);
        };
        model.addAttribute("resultados", resultados);
        model.addAttribute("termo", termo);
        model.addAttribute("filtro", filtro);
        return "consulta-livro";
    }

    // ── GET /perfil-cliente ──────────────────────────────────────────────────────
    @GetMapping("/perfil-cliente")
    public String perfilCliente(@RequestParam(required = false) String cpf, Model model) {
        if (cpf != null && !cpf.isBlank()) {
            try {
                var cliente = clienteService.buscarPorCpf(cpf);
                var historico = circulacaoService.listarHistoricoDoCliente(cpf);
                model.addAttribute("cliente", cliente);
                model.addAttribute("historico", historico);
                model.addAttribute("cpf", cpf);
            } catch (Exception e) {
                model.addAttribute("erro", "Cliente com CPF " + cpf + " não encontrado.");
            }
        }
        return "perfil-cliente";
    }
}