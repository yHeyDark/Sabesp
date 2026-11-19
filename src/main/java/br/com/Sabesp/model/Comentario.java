package br.com.Sabesp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "comentario")
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comentario")
    private Long idComentario;

    @Column(name = "data_comentario", nullable = false)
    private LocalDateTime dataComentario;

    @Column(name = "texto", nullable = false, columnDefinition = "TEXT")
    private String texto;

    // FK → Chamado (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_chamado", nullable = false)
    private Chamado chamado;

    // FK → Usuario (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
}

