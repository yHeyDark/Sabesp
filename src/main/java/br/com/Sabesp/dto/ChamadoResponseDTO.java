package br.com.Sabesp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChamadoResponseDTO {

    private Long idChamado;
    private LocalDate dataChamado;
    private LocalTime horaChamado;
    private String descricao;
    private StatusChamadoDTO status;
    private UsuarioResponseDTO usuario;
    private EnderecoResponseDTO endereco;
    private TipoProblemaDTO tipoProblema;
    private String imagemUrl;
}

