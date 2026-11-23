package br.com.Sabesp.model.enums;

/**
 * Enum com os perfis de acesso (RBAC) do sistema.
 *
 * <ul>
 *   <li>{@link #COMUM} — cidadão: abre e acompanha os próprios chamados</li>
 *   <li>{@link #SABESP} — equipe técnica: altera status, comenta e gerencia cadastros</li>
 *   <li>{@link #PREFEITURA} — fiscalização: audita/filtra chamados e cria contas institucionais</li>
 * </ul>
 *
 * <p>Esse perfil é enviado pelo front-end no cabeçalho {@code X-User-Role}
 * e usado pelo backend para autorizar cada operação.</p>
 *
 * @author Equipe Sabesp
 * @since 2026
 */
public enum TipoUsuario {
    COMUM,
    SABESP,
    PREFEITURA
}

