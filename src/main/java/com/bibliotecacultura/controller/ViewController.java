package com.bibliotecacultura.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/login")
    public String paginaLogin() {
        return "login";
    }
    @GetMapping("/cadastro-funcionario")
    public String paginaCadastroFuncionario() {
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
    public String RedefinirSenha() {
        return "redefinir-senha"; 
    }
    @GetMapping("/homescreen")
    public String HomeScreen() {
        return "homescreen"; 
    }
    @GetMapping("/realizar-emprestimo")
    public String RealizarEmprestimo() {
        return "realizar-emprestimo"; 
    }
        @GetMapping("/consulta-funcionario")
    public String ConsultaFuncionario() {
        return "consulta-funcionario"; 
    }
}