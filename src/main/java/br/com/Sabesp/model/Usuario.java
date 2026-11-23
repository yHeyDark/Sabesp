package br.com.Sabesp.model;

import br.com.Sabesp.model.enums.TipoUsuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade que representa um usuário do sistema de chamados de esgoto da
 * Secretaria de Meio Ambiente de Santos. Um usuário pode ter o perfil de
 * Cidadão (COMUM), que abre e acompanha chamados, de Sabesp, que executa os
 * atendimentos, ou de Prefeitura, que monitora e filtra os chamados do
 * município.
 *
 * @author Equipe Sabesp
 * @since 2026
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "email", nullable = false, unique = true, length = 200)
    private String email;

    @Column(name = "senha", nullable = false, length = 255)
    private String senha;

    @Column(name = "telefone", length = 20)
    private String telefone;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_usuario", nullable = false, length = 20)
    private TipoUsuario tipoUsuario;

    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro;

    // Um Usuário possui muitos Chamados (1:N)
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Chamado> chamados = new ArrayList<>();

    // Um Usuário possui muitos Comentários (1:N)
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comentario> comentarios = new ArrayList<>();
}

