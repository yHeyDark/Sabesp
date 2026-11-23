package br.com.Sabesp.repository;

import br.com.Sabesp.model.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório de {@link Comentario} (comentários/interações em um chamado).
 *
 * <p>Permite listar os comentários associados a um chamado específico.</p>
 *
 * @author Equipe Sabesp
 */
@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    /**
     * Busca todos os comentários de um chamado.
     *
     * @param idChamado identificador do chamado
     * @return lista de comentários do chamado informado
     */
    List<Comentario> findByChamadoIdChamado(Long idChamado);
}

