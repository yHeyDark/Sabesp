package br.com.Sabesp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de entrada (Request) para criação de um comentário em um chamado de esgoto.
 * Transporta os dados enviados pelo usuário à API REST; o comentário fica associado
 * ao chamado indicado e registrado em nome do usuário informado.
 *
 * @author Equipe Sabesp
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComentarioRequestDTO {

    /** Conteúdo do comentário. */
    private String texto;
    /** Identificador do chamado ao qual o comentário será anexado. */
    private Long idChamado;
    /** Identificador do usuário autor do comentário. */
    private Long idUsuario;
}

