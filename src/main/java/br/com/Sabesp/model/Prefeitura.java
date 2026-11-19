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
@Table(name = "prefeitura")
public class Prefeitura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prefeitura")
    private Long idPrefeitura;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "email", nullable = false, unique = true, length = 200)
    private String email;

    @Column(name = "senha", nullable = false, length = 255)
    private String senha;

    // Uma Prefeitura possui muitos Filtros de Pesquisa (1:N)
    @OneToMany(mappedBy = "prefeitura", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FiltroPesquisa> filtrosPesquisa = new ArrayList<>();
}

