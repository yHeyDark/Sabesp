package br.com.Sabesp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO de entrada (Request) utilizado para criar ou atualizar um chamado de esgoto.
 * Transporta os dados enviados pelo usuário na API REST de chamados da cidade de Santos.
 * Os campos de referência (status, usuário, endereço e tipo de problema) são informados
 * por ID, pois apontam para registros já existentes no banco.
 *
 * @author Equipe Sabesp
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChamadoRequestDTO {

    /** Data em que o chamado foi registrado. */
    private LocalDate dataChamado;
    /** Hora em que o chamado foi registrado. */
    private LocalTime horaChamado;
    /** Descrição do problema relatado pelo usuário. */
    private String descricao;
    /** Identificador do status atual do chamado (referência para {@link StatusChamadoDTO}). */
    private Long idStatus;
    /** Identificador do usuário autor do chamado. */
    private Long idUsuario;
    /** Identificador do endereço onde ocorre o problema (ver {@link EnderecoRequestDTO}). */
    private Long idEndereco;
    /** Identificador da categoria do problema (ver {@link TipoProblemaDTO}). */
    private Long idTipoProblema;
    /** URL da imagem anexada ao chamado como evidência do problema. */
    private String imagemUrl;
}

