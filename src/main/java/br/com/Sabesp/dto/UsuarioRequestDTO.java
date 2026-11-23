package br.com.Sabesp.dto;

import br.com.Sabesp.model.enums.TipoUsuario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de requisição para criar/atualizar um usuário.
 *
 * <p>Usado nos endpoints de {@code UsuarioController} e no auto-cadastro
 * de cidadãos ({@code POST /api/auth/registro}). O campo
 * {@link #tipoUsuario} define o perfil: {@code COMUM}, {@code SABESP} ou
 * {@code PREFEITURA} — a criação de perfis institucionais exige perfil
 * {@code PREFEITURA} no cabeçalho {@code X-User-Role} (regra RBAC).</p>
 *
 * @author Equipe Sabesp
 * @since 2026
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRequestDTO {

    /** Nome completo do usuário. */
    private String nome;

    /** E-mail (usado como login, deve ser único). */
    private String email;

    /** Senha de acesso. */
    private String senha;

    /** Telefone para contato (opcional). */
    private String telefone;

    /** Perfil de acesso: COMUM, SABESP ou PREFEITURA. */
    private TipoUsuario tipoUsuario;
}

