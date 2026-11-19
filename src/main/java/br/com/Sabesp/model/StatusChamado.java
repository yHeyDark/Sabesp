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
@Table(name = "status_chamado")
public class StatusChamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_status")
    private Long idStatus;

    @Column(name = "descricao", nullable = false, unique = true, length = 50)
    private String descricao;

    // Um StatusChamado possui muitos Chamados (1:N)
    @OneToMany(mappedBy = "statusChamado", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Chamado> chamados = new ArrayList<>();
}

