package br.com.Sabesp.repository;

import br.com.Sabesp.model.StatusChamado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositório de {@link StatusChamado} (status possíveis de um chamado).
 *
 * <p>Fornece consulta para buscar um status pela sua descrição.</p>
 *
 * @author Equipe Sabesp
 */
@Repository
public interface StatusChamadoRepository extends JpaRepository<StatusChamado, Long> {

    /**
     * Busca um status de chamado pela sua descrição.
     *
     * @param descricao descrição do status (ex.: "ABERTO", "EM ANÁLISE", "RESOLVIDO")
     * @return {@link Optional} contendo o status, ou vazio se não encontrado
     */
    Optional<StatusChamado> findByDescricao(String descricao);
}

