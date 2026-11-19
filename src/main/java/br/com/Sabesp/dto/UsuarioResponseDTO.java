package br.com.Sabesp.dto;

import br.com.Sabesp.model.enums.TipoUsuario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDTO {

    private Long idUsuario;
    private String nome;
    private String email;
    private String telefone;
    private TipoUsuario tipoUsuario;
    private LocalDateTime dataCadastro;
}

