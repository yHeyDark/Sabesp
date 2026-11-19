package br.com.Sabesp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FiltroPesquisaResponseDTO {

    private Long idFiltro;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String tipoProblema;
    private String status;
    private String bairro;
    private String cidade;
    private String outrosCriterios;
    private Long idPrefeitura;
}

