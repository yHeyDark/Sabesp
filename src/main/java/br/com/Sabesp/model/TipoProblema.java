package br.com.Sabesp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tipo_problema")
public class TipoProblema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_problema")
    private Long idTipoProblema;

    @Column(name = "descricao", nullable = false, length = 255)
    private String descricao;

    // Um TipoProblema possui muitos Chamados (1:N)
    @OneToMany(mappedBy = "tipoProblema", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Chamado> chamados = new ArrayList<>();
}

