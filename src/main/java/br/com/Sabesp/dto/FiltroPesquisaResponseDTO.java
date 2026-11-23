package br.com.Sabesp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO de saída (Response) retornado pela API REST com um filtro de pesquisa
 * de chamados de esgoto já persistido. Reproduz os critérios aplicados
 * (período, tipo de problema, status e região) para posterior reutilização.
 *
 * @author Equipe Sabesp
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FiltroPesquisaResponseDTO {

    /** Identificador único do filtro salvo. */
    private Long idFiltro;
    /** Data inicial do período filtrado. */
    private LocalDate dataInicio;
    /** Data final do período filtrado. */
    private LocalDate dataFim;
    /** Categoria do problema utilizada no filtro. */
    private String tipoProblema;
    /** Situação do chamado utilizada no filtro. */
    private String status;
    /** Bairro utilizado no filtro, quando informado. */
    private String bairro;
    /** Cidade utilizada no filtro, quando informada. */
    private String cidade;
    /** Critérios complementares livres aplicados. */
    private String outrosCriterios;
    /** Identificador da prefeitura dona do filtro, quando aplicável. */
    private Long idPrefeitura;
}

