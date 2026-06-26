package com.bibliotecacultura.session;

import com.bibliotecacultura.entity.Funcionario;
import java.io.Serializable;

/**
 * Objeto leve que fica guardado na HttpSession após o login.
 * Evita re-carregar o funcionário do banco a cada request.
 */
public class SessaoFuncionario implements Serializable {

    private final Long id;
    private final String nome;
    private final String matricula;
    private final Funcionario.Cargo cargo;

    public SessaoFuncionario(Long id, String nome, String matricula, Funcionario.Cargo cargo) {
        this.id        = id;
        this.nome      = nome;
        this.matricula = matricula;
        this.cargo     = cargo;
    }

    public Long getId()                  { return id; }
    public String getNome()              { return nome; }
    public String getMatricula()         { return matricula; }
    public Funcionario.Cargo getCargo()  { return cargo; }

    /** Usado nos templates Thymeleaf: [[${sessao.cargoLabel}]] */
    public String getCargoLabel()        { return cargo.getLabel(); }

    /** Usado para proteger rotas ADM nos templates e controllers */
    public boolean isAdm()               { return cargo.isAdm(); }
}