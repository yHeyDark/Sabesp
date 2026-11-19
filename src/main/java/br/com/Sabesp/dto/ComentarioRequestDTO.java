package br.com.Sabesp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComentarioRequestDTO {

    private String texto;
    private Long idChamado;
    private Long idUsuario;
}

