package br.com.Sabesp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de entrada (Request) com os dados de um endereço, utilizado na criação ou
 * atualização de endereços associados a chamados de esgoto da cidade de Santos.
 * Os campos obrigatórios (logradouro, número, bairro, cidade, estado e CEP)
 * devem ser informados pelo usuário.
 *
 * @author Equipe Sabesp
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnderecoRequestDTO {

    /** Rua ou avenida do endereço. */
    private String logradouro;
    /** Número do imóvel. */
    private String numero;
    /** Complemento do endereço (opcional, ex.: bloco, apartamento). */
    private String complemento;
    /** Bairro do imóvel, utilizado nos filtros de pesquisa por região. */
    private String bairro;
    /** Cidade do imóvel (para chamados da rede de Santos). */
    private String cidade;
    /** Unidade federativa do imóvel (ex.: SP). */
    private String estado;
    /** CEP do imóvel, no formato 00000-000. */
    private String cep;
}

