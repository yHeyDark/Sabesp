package br.com.Sabesp.model.enums;

/**
 * Enum com os possíveis status de um chamado de esgoto.
 *
 * <p>Representa as fases do fluxo de atendimento:</p>
 * <ul>
 *   <li>{@link #ABERTO} — chamado recém-criado pelo cidadão (status inicial forçado)</li>
 *   <li>{@link #EM_ANDAMENTO} — equipe Sabesp acionada e em campo</li>
 *   <li>{@link #RESOLVIDO} — problema sanado</li>
 *   <li>{@link #FECHADO} — atendimento encerrado e auditado</li>
 * </ul>
 *
 * <p>Apenas o perfil {@code SABESP} pode transitar entre esses status.</p>
 *
 * @author Equipe Sabesp
 * @since 2026
 */
public enum StatusChamadoEnum {
    ABERTO,
    EM_ANDAMENTO,
    RESOLVIDO,
    FECHADO
}

