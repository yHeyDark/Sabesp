package br.com.Sabesp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de um tipo de problema de esgoto (ex.: "Vazamento", "Entupimento").
 *
 * <p>Categoria escolhida pelo cidadão ao abrir um chamado, usada para
 * direcionar o atendimento e para os filtros de consulta da Prefeitura.</p>
 *
 * @author Equipe Sabesp
 * @since 2026
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TipoProblemaDTO {

    /** Identificador único do tipo de problema. */
    private Long idTipoProblema;

    /** Descrição do problema (ex.: "Entupimento de bueiro"). */
    private String descricao;
}

