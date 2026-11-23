package br.com.Sabesp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidade de referência que representa o status de um chamado no domínio do
 * sistema. É a tabela persistida usada como chave estrangeira nos chamados,
 * com uma descrição única que identifica em que etapa do atendimento o
 * chamado se encontra (aberto, em andamento, resolvido ou fechado).
 *
 * @author Equipe Sabesp
 * @since 2026
 */
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

