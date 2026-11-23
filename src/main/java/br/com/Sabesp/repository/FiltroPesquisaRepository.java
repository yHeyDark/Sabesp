package br.com.Sabesp.repository;

import br.com.Sabesp.model.FiltroPesquisa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório de {@link FiltroPesquisa} (filtros de pesquisa salvos por prefeitura).
 *
 * <p>Permite listar os filtros associados a uma prefeitura específica.</p>
 *
 * @author Equipe Sabesp
 */
@Repository
public interface FiltroPesquisaRepository extends JpaRepository<FiltroPesquisa, Long> {

    /**
     * Busca todos os filtros de pesquisa de uma prefeitura.
     *
     * @param idPrefeitura identificador da prefeitura
     * @return lista de filtros da prefeitura informada
     */
    List<FiltroPesquisa> findByPrefeituraIdPrefeitura(Long idPrefeitura);
}

