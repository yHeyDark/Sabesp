package br.com.Sabesp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de requisição para autenticação de usuário (login).
 *
 * <p>Transporta as credenciais enviadas pelo front-end para o endpoint
 * {@code POST /api/auth/login}. O backend valida se o e-mail existe e se a
 * senha confere antes de liberar a sessão.</p>
 *
 * @author Equipe Sabesp
 * @since 2026
 */
@Data // Lombok: gera getters, setters, equals, hashCode e toString
@NoArgsConstructor // Lombok: construtor sem argumentos (exigido pelo Jackson)
@AllArgsConstructor // Lombok: construtor com todos os campos
public class LoginRequestDTO {

    /** E-mail cadastrado do usuário. */
    private String email;

    /** Senha em texto puro (validada no backend contra o banco). */
    private String senha;
}

