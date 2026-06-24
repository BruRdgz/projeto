package com.bibliotecacultura.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.bibliotecacultura.entity.Funcionario;
import com.bibliotecacultura.entity.Livro;
import com.bibliotecacultura.repository.LivroRepository;

@Controller
public class ViewController {

    @Autowired
    private LivroRepository livroRepository;

    @GetMapping("/login")
    public String paginaLogin() {
        return "login";
    }

    @GetMapping("/cadastro-funcionario")
    public String paginaCadastroFuncionario(Model model) {
        model.addAttribute("funcionario", new Funcionario());
        return "cadastro-funcionario";
    }

    @GetMapping("/esqueci-senha")
    public String paginaEsqueciSenha() {
        return "esqueci-senha";
    }

    @GetMapping("/visualizar-acervo")
    public String paginaVisualizarAcervo() {
        return "visualizar-acervo";
    }

    @GetMapping("/redefinir-senha")
    public String redefinirSenha() {
        return "redefinir-senha";
    }

    @GetMapping("/homescreen")
    public String homeScreen() {
        return "homescreen";
    }

    @GetMapping("/busca")
    public String buscarLivros(@RequestParam(defaultValue = "") String termo, Model model) {
        var resultados = livroRepository.search(termo);
        model.addAttribute("termo", termo);
        model.addAttribute("resultados", resultados);
        return "busca";
    }

    @GetMapping("/realizar-emprestimo")
    public String realizarEmprestimo() {
        return "realizar-emprestimo";
    }

    @GetMapping("/consulta-funcionario")
    public String consultaFuncionario() {
        return "consulta-funcionario";
    }

    @GetMapping("/consulta-livro")
    public String consultaLivro(
            @RequestParam(defaultValue = "") String termo,
            @RequestParam(defaultValue = "titulo") String filtro,
            Model model) {

        List<Livro> resultados = switch (filtro) {
            case "autor"  -> livroRepository.searchByAutor(termo);
            case "genero" -> livroRepository.searchByGenero(termo);
            default       -> livroRepository.searchByTitulo(termo);
        };

        model.addAttribute("termo", termo);
        model.addAttribute("filtro", filtro);
        model.addAttribute("resultados", resultados);
        return "consulta-livro";
    }

    @GetMapping("/perfil-cliente")
    public String perfilCliente() {
        return "perfil-cliente";
    }
}