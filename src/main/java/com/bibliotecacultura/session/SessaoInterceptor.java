package com.bibliotecacultura.session;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Intercepta todas as requisições:
 *  1. Redireciona para /login se não houver sessão ativa (exceto rotas públicas).
 *  2. Bloqueia rotas ADM se o funcionário não tiver cargo BIBLIOTECARIO_ADM.
 *  3. Injeta o objeto "sessao" no Model de cada resposta de template.
 */
public class SessaoInterceptor implements HandlerInterceptor {

    /** Rotas que não precisam de autenticação */
    private static final String[] ROTAS_PUBLICAS = {
            "/login", "/esqueci-senha", "/redefinir-senha",
            "/css/", "/images/", "/js/", "/favicon.ico"
    };

    /** Rotas que só o BIBLIOTECARIO_ADM pode acessar */
    private static final String[] ROTAS_ADM = {
            "/funcionarios", "/cadastro-funcionario"
    };

    @Override
    public boolean preHandle(HttpServletRequest req,
                             HttpServletResponse res,
                             Object handler) throws Exception {

        String path = req.getRequestURI();

        // Deixa rotas públicas passarem sempre
        for (String pub : ROTAS_PUBLICAS) {
            if (path.startsWith(pub)) return true;
        }

        SessaoFuncionario sessao = (SessaoFuncionario)
                req.getSession(false) == null ? null
                : (SessaoFuncionario) req.getSession().getAttribute("sessao");

        // Sem sessão → redireciona para login
        if (sessao == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return false;
        }

        // Rota ADM → verifica cargo
        for (String adm : ROTAS_ADM) {
            if (path.startsWith(adm) && !sessao.isAdm()) {
                res.sendRedirect(req.getContextPath() + "/homescreen?acesso=negado");
                return false;
            }
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest req,
                           HttpServletResponse res,
                           Object handler,
                           ModelAndView mav) {
        if (mav == null) return;

        // Injeta a sessão no model para que qualquer template possa usar ${sessao}
        SessaoFuncionario sessao = req.getSession(false) == null ? null
                : (SessaoFuncionario) req.getSession().getAttribute("sessao");
        if (sessao != null) {
            mav.addObject("sessao", sessao);
        }
    }
}