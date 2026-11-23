package br.com.Sabesp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de saída (Response) retornado pela API REST com os dados de um endereço
 * vinculado a um chamado de esgoto. Além dos campos do endereço, inclui o
 * identificador gerado para o registro.
 *
 * @author Equipe Sabesp
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnderecoResponseDTO {

    /** Identificador único do endereço. */
    private Long idEndereco;
    /** Rua ou avenida do endereço. */
    private String logradouro;
    /** Número do imóvel. */
    private String numero;
    /** Complemento do endereço, quando informado. */
    private String complemento;
    /** Bairro do imóvel. */
    private String bairro;
    /** Cidade do imóvel. */
    private String cidade;
    /** Unidade federativa do imóvel (ex.: SP). */
    private String estado;
    /** CEP do imóvel, no formato 00000-000. */
    private String cep;
}

