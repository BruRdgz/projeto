package com.bibliotecacultura.session;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SessaoInterceptor())
                // exclui recursos estáticos para não bloquear CSS/imagens
                .excludePathPatterns(
                        "/css/**", "/js/**", "/images/**", "/favicon.ico"
                );
    }
}