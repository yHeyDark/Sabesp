package br.com.Sabesp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO de saída (Response) retornado pela API REST de chamados de esgoto (cidade de Santos).
 * Transporta os dados completos de um chamado, com os objetos relacionados (status,
 * usuário, endereço e tipo de problema) já resolvidos em seus respectivos DTOs,
 * dispensando consultas adicionais pelo cliente.
 *
 * @author Equipe Sabesp
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChamadoResponseDTO {

    /** Identificador único do chamado. */
    private Long idChamado;
    /** Data em que o chamado foi registrado. */
    private LocalDate dataChamado;
    /** Hora em que o chamado foi registrado. */
    private LocalTime horaChamado;
    /** Descrição do problema relatado. */
    private String descricao;
    /** Situação atual do chamado (ex.: aberto, em andamento, resolvido). */
    private StatusChamadoDTO status;
    /** Usuário autor do chamado. */
    private UsuarioResponseDTO usuario;
    /** Endereço onde o problema foi relatado. */
    private EnderecoResponseDTO endereco;
    /** Categoria do problema relatado. */
    private TipoProblemaDTO tipoProblema;
    /** URL da imagem anexada ao chamado, quando houver. */
    private String imagemUrl;
}

