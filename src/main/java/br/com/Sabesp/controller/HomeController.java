package br.com.Sabesp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador MVC responsável por redirecionar (forward) rotas raiz para páginas HTML estáticas.
 *
 * <p>Diferente dos demais controladores do sistema, este utiliza {@code @Controller}
 * em vez de {@code @RestController}, pois os métodos retornam nomes de view/forward
 * (Strings interpretadas pelo ViewResolver do Spring) em vez de objetos serializados
 * em JSON.</p>
 *
 * <p>O forward ({@code "forward:/..."}) mantém a URL original no navegador do usuário
 * enquanto serve o conteúdo do arquivo HTML alvo internamente no servidor,
 * diferentemente de um redirect, que alteraria a URL visível.</p>
 */
@Controller // @Controller (não @RestController): retorna nomes de view, não JSON
public class HomeController {

    /**
     * Encaminha a raiz da aplicação ({@code /}) para a página inicial {@code index.html}.
     *
     * <p><b>Endpoint:</b> {@code GET /}</p>
     * <p>Quando o usuário acessa a URL raiz, o Spring faz um forward interno
     * para {@code /index.html}, servindo o arquivo estático sem redirecionar o navegador.</p>
     *
     * @return String de forward para {@code /index.html}
     */
    @GetMapping("/") // Mapeia a requisição GET na raiz da aplicação
    public String index() {
        // "forward:" instrui o Spring a encaminhar internamente para /index.html
        // sem alterar a URL no navegador do cliente (diferente de "redirect:")
        return "forward:/index.html";
    }

    /**
     * Encaminha a rota {@code /chamados} para a página de chamados {@code chamados.html}.
     *
     * <p><b>Endpoint:</b> {@code GET /chamados}</p>
     * <p>Permite que a URL amigável {@code /chamados} sirva a página estática
     * {@code chamados.html} via forward interno.</p>
     *
     * @return String de forward para {@code /chamados.html}
     */
    @GetMapping("/chamados") // Mapeia GET /chamados
    public String chamados() {
        // Forward interno para /chamados.html — serve o HTML sem redirect
        return "forward:/chamados.html";
    }
}
