package br.com.Sabesp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChamadoRequestDTO {

    private LocalDate dataChamado;
    private LocalTime horaChamado;
    private String descricao;
    private Long idStatus;
    private Long idUsuario;
    private Long idEndereco;
    private Long idTipoProblema;
    private String imagemUrl;
}

