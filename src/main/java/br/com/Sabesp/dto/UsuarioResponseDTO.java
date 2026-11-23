package br.com.Sabesp.dto;

import br.com.Sabesp.model.enums.TipoUsuario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de resposta com os dados públicos de um usuário.
 *
 * <p>Retornado pelos endpoints de login, registro e consulta de usuários.
 * A senha nunca é incluída nesta resposta.</p>
 *
 * @author Equipe Sabesp
 * @since 2026
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDTO {

    /** Identificador único do usuário. */
    private Long idUsuario;

    /** Nome completo exibido na interface. */
    private String nome;

    /** E-mail (login do usuário). */
    private String email;

    /** Telefone para contato (pode ser nulo). */
    private String telefone;

    /** Perfil de acesso: COMUM, SABESP ou PREFEITURA. */
    private TipoUsuario tipoUsuario;

    /** Data e hora em que a conta foi criada. */
    private LocalDateTime dataCadastro;
}

