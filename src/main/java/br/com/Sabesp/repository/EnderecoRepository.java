package br.com.Sabesp.repository;

import br.com.Sabesp.model.Endereco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório de {@link Endereco} (endereços dos chamados de esgoto).
 *
 * <p>Fornece consultas derivadas para filtrar endereços por bairro e por cidade.</p>
 *
 * @author Equipe Sabesp
 */
@Repository
public interface EnderecoRepository extends JpaRepository<Endereco, Long> {

    /**
     * Busca todos os endereços de um determinado bairro.
     *
     * @param bairro nome do bairro
     * @return lista de endereços do bairro informado
     */
    List<Endereco> findByBairro(String bairro);

    /**
     * Busca todos os endereços de uma determinada cidade.
     *
     * @param cidade nome da cidade
     * @return lista de endereços da cidade informada
     */
    List<Endereco> findByCidade(String cidade);
}

