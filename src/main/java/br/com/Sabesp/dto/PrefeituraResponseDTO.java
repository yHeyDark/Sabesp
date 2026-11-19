package br.com.Sabesp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrefeituraResponseDTO {

    private Long idPrefeitura;
    private String nome;
    private String email;
}

