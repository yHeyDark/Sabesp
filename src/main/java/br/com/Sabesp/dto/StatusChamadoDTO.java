package br.com.Sabesp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusChamadoDTO {

    private Long idStatus;
    private String descricao;
}

