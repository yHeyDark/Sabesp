package br.com.Sabesp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para a solicitação de recuperação de senha.
 * O usuário informa o e-mail da conta e a nova senha desejada.
 */
public class RecuperarSenhaDTO {

    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "Informe um e-mail válido.")
    private String email;

    @NotBlank(message = "A nova senha é obrigatória.")
    @Size(min = 6, max = 50, message = "A senha deve ter entre 6 e 50 caracteres.")
    private String novaSenha;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNovaSenha() {
        return novaSenha;
    }

    public void setNovaSenha(String novaSenha) {
        this.novaSenha = novaSenha;
    }
}
