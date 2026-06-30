package com.bibliotecacultura.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bibliotecacultura.entity.Funcionario;
import com.bibliotecacultura.entity.Livro;
import com.bibliotecacultura.exception.NegocioException;
import com.bibliotecacultura.dto.LivroDTO;
import com.bibliotecacultura.repository.LivroRepository;
import com.bibliotecacultura.service.AcervoService;
import com.bibliotecacultura.service.ClienteService;
import com.bibliotecacultura.service.FuncionarioService;
import com.bibliotecacultura.session.SessaoFuncionario;

import jakarta.servlet.http.HttpSession;

@Controller
public class ViewController {

    @Autowired private FuncionarioService funcionarioService;
    @Autowired private ClienteService clienteService;
    @Autowired private AcervoService acervoService;
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

    // ── GET /realizar-devolucao ─────────────────────────────────────────────────
    @GetMapping("/realizar-devolucao")
    public String realizarDevolucao() {
        return "realizar-devolucao";
    }

    // ── GET /consulta-funcionario ────────────────────────────────────────────────
    @GetMapping("/consulta-funcionario")
    public String consultaFuncionario(@RequestParam(defaultValue = "") String q, Model model) {
        model.addAttribute("funcionarios", funcionarioService.listarTodos(q));
        model.addAttribute("q", q);
        return "consulta-funcionario";
    }

    // ── GET /cadastro-livro ────────────────────────────────────────────────
    @GetMapping("/cadastro-livro")
    public String cadastroLivro(Model model) {
        model.addAttribute("livro", new LivroDTO());
        model.addAttribute("categorias", acervoService.listarCategorias());
        return "cadastro-livro";
    }

    // ── POST /cadastro-livro ───────────────────────────────────────────────
    @PostMapping("/cadastro-livro")
    public String salvarLivro(@RequestParam String titulo,
                              @RequestParam Integer anoPublicacao,
                              @RequestParam String autor,
                              @RequestParam Long categoriaId,
                              RedirectAttributes ra) {
        try {
            LivroDTO dto = new LivroDTO();
            dto.setTitulo(titulo);
            dto.setAnoPublicacao(anoPublicacao);
            dto.setAutor(autor);
            dto.setCategoriaId(categoriaId);
            acervoService.cadastrar(dto);
            ra.addFlashAttribute("sucesso", "Livro cadastrado com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Não foi possível concluir a operação.");
            return "redirect:/cadastro-livro";
        }

        return "redirect:/visualizar-acervo";
    }

    // ── GET /cadastro-exemplar ────────────────────────────────────────────────
    @GetMapping("/cadastro-exemplar")
    public String cadastroExemplar(@RequestParam(defaultValue = "") String termo, Model model) {
        if (!termo.isBlank()) {
            model.addAttribute("livros", livroRepository.search(termo));
        }
        model.addAttribute("termo", termo);
        return "cadastro-exemplar";
    }

    // ── POST /cadastro-exemplar ───────────────────────────────────────────────
    @PostMapping("/cadastro-exemplar")
    public String salvarExemplar(@RequestParam Long livroId, RedirectAttributes ra) {
        try {
            acervoService.cadastrarExemplar(livroId);
            ra.addFlashAttribute("sucesso", "Exemplar cadastrado com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Não foi possível concluir a operação.");
            return "redirect:/cadastro-exemplar";
        }

        return "redirect:/visualizar-acervo";
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
            ra.addFlashAttribute("erro", "Não foi possível concluir a operação.");
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
                model.addAttribute("erro", "Não foi possível concluir a operação.");
            }
        }
        return "perfil-cliente";
    }
}
