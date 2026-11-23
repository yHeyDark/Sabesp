package br.com.Sabesp.repository;

import br.com.Sabesp.model.Chamado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório de {@link Chamado} (chamados de problemas de esgoto).
 *
 * <p>Fornece consultas derivadas (Spring Data JPA) para buscar chamados por
 * usuário, status e bairro, além de contagem por status.</p>
 *
 * @author Equipe Sabesp
 */
@Repository
public interface ChamadoRepository extends JpaRepository<Chamado, Long> {

    /**
     * Busca todos os chamados registrados por um usuário.
     *
     * @param idUsuario identificador do usuário
     * @return lista de chamados do usuário
     */
    List<Chamado> findByUsuarioIdUsuario(Long idUsuario);

    /**
     * Busca todos os chamados com um determinado status.
     *
     * @param idStatus identificador do status do chamado
     * @return lista de chamados com o status informado
     */
    List<Chamado> findByStatusChamadoIdStatus(Long idStatus);

    /**
     * Busca todos os chamados de um determinado bairro.
     *
     * @param bairro nome do bairro
     * @return lista de chamados do bairro informado
     */
    List<Chamado> findByEnderecoBairro(String bairro);

    /**
     * Conta a quantidade de chamados com o status descrito.
     *
     * @param descricao descrição do status (ex.: "ABERTO", "RESOLVIDO")
     * @return número de chamados com o status informado
     */
    long countByStatusChamadoDescricao(String descricao);
}

