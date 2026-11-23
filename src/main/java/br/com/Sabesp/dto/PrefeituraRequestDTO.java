package br.com.Sabesp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de requisição para criação de cadastro da Prefeitura.
 *
 * <p>Usado pelos endpoints de {@code PrefeituraController} quando a
 * Secretaria de Meio Ambiente cadastra um novo acesso de fiscalização.</p>
 *
 * @author Equipe Sabesp
 * @since 2026
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrefeituraRequestDTO {

    /** Nome completo ou nome funcional da pessoa/fiscal. */
    private String nome;

    /** E-mail institucional (usado como login). */
    private String email;

    /** Senha de acesso da conta. */
    private String senha;
}

