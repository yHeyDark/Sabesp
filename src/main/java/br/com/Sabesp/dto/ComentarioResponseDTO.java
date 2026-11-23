package br.com.Sabesp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de saída (Response) retornado pela API REST ao consultar comentários de um
 * chamado de esgoto. Transporta os dados do comentário, incluindo o autor já
 * resolvido como {@link UsuarioResponseDTO}.
 *
 * @author Equipe Sabesp
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComentarioResponseDTO {

    /** Identificador único do comentário. */
    private Long idComentario;
    /** Data e hora em que o comentário foi registrado. */
    private LocalDateTime dataComentario;
    /** Conteúdo do comentário. */
    private String texto;
    /** Identificador do chamado ao qual o comentário pertence. */
    private Long idChamado;
    /** Usuário autor do comentário. */
    private UsuarioResponseDTO usuario;
}

