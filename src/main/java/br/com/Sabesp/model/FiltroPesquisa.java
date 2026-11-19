package br.com.Sabesp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "filtro_pesquisa")
public class FiltroPesquisa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_filtro")
    private Long idFiltro;

    @Column(name = "data_inicio")
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Column(name = "tipo_problema", length = 255)
    private String tipoProblema;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "bairro", length = 100)
    private String bairro;

    @Column(name = "cidade", length = 100)
    private String cidade;

    @Column(name = "outros_criterios", columnDefinition = "TEXT")
    private String outrosCriterios;

    // FK → Prefeitura (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_prefeitura", nullable = false)
    private Prefeitura prefeitura;
}

