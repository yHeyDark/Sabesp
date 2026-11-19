package br.com.Sabesp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComentarioResponseDTO {

    private Long idComentario;
    private LocalDateTime dataComentario;
    private String texto;
    private Long idChamado;
    private UsuarioResponseDTO usuario;
}

