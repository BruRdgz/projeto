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
}