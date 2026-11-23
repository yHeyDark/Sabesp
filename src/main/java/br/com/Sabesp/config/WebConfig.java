package br.com.Sabesp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração de CORS da aplicação.
 *
 * <p>Libera o acesso às rotas da API ({@code /api/**}) para o front-end
 * estático, permitindo todos os métodos e cabeçalhos.</p>
 *
 * @author Equipe Sabesp
 */
@Configuration
public class WebConfig {

    /**
     * Cria o bean que configura as regras de CORS da aplicação.
     *
     * @return {@link WebMvcConfigurer} que libera a origem, os métodos e os
     *         cabeçalhos para o mapeamento {@code /api/**}
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }
}

