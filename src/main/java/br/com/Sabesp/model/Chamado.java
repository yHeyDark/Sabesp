package br.com.Sabesp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade que representa um chamado (relato) de problema de esgoto aberto
 * por um cidadão no município de Santos. Armazena a data, a hora, a
 * descrição e a foto do problema relatado, além das referências para o
 * usuário que abriu, o endereço onde ocorreu, o tipo de problema e o status
 * atual do atendimento.
 *
 * @author Equipe Sabesp
 * @since 2026
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chamado")
public class Chamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_chamado")
    private Long idChamado;

    @Column(name = "data_chamado", nullable = false)
    private LocalDate dataChamado;

    @Column(name = "hora_chamado", nullable = false)
    private LocalTime horaChamado;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "imagem_url", columnDefinition = "TEXT")
    private String imagemUrl;

    // FK → StatusChamado (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_status", nullable = false)
    private StatusChamado statusChamado;

    // FK → Usuario (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    // FK → Endereco (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_endereco", nullable = false)
    private Endereco endereco;

    // FK → TipoProblema (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_problema", nullable = false)
    private TipoProblema tipoProblema;

    // Um Chamado possui muitos Comentários (1:N)
    @OneToMany(mappedBy = "chamado", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comentario> comentarios = new ArrayList<>();
}

