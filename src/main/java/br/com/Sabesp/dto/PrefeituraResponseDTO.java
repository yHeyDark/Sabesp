package br.com.Sabesp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de resposta do cadastro da Prefeitura.
 *
 * <p>Retorna os dados públicos de um cadastro de fiscalização.
 * A senha nunca é incluída nesta resposta por segurança.</p>
 *
 * @author Equipe Sabesp
 * @since 2026
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrefeituraResponseDTO {

    /** Identificador único do cadastro de prefeitura. */
    private Long idPrefeitura;

    /** Nome do fiscal/cadastro. */
    private String nome;

    /** E-mail institucional do cadastro. */
    private String email;
}

