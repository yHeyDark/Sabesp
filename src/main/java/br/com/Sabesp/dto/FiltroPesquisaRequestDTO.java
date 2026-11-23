package br.com.Sabesp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO de entrada (Request) com os critérios de pesquisa/relatório de chamados
 * de esgoto. Transporta os filtros enviados pelo usuário à API REST; todos os
 * campos são opcionais e combinam-se para restringir os resultados.
 *
 * @author Equipe Sabesp
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FiltroPesquisaRequestDTO {

    /** Data inicial do período desejado (inclusiva). */
    private LocalDate dataInicio;
    /** Data final do período desejado (inclusiva). */
    private LocalDate dataFim;
    /** Categoria do problema a ser filtrada (ex.: esgoto entupido). */
    private String tipoProblema;
    /** Situação do chamado a ser filtrada (ex.: aberto, em andamento). */
    private String status;
    /** Bairro dos chamados, para filtro por região. */
    private String bairro;
    /** Cidade dos chamados, para filtro por município. */
    private String cidade;
    /** Critérios complementares livres de pesquisa. */
    private String outrosCriterios;
    /** Identificador da prefeitura dona do escopo de pesquisa, quando aplicável. */
    private Long idPrefeitura;
}

