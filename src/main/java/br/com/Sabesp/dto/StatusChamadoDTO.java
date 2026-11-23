package br.com.Sabesp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de um status de chamado (ex.: "Aberto", "Em andamento").
 *
 * <p>Representa as opções de situação que um chamado pode ter durante
 * o seu fluxo de atendimento pela equipe Sabesp.</p>
 *
 * @author Equipe Sabesp
 * @since 2026
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusChamadoDTO {

    /** Identificador único do status. */
    private Long idStatus;

    /** Descrição exibida do status (ex.: "Em andamento"). */
    private String descricao;
}

