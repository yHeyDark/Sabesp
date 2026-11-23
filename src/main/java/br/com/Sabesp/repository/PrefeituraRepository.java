package br.com.Sabesp.repository;

import br.com.Sabesp.model.Prefeitura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositório de {@link Prefeitura} (prefeituras parceiras da Sabesp).
 *
 * <p>Fornece consulta para buscar uma prefeitura pelo e-mail de acesso.</p>
 *
 * @author Equipe Sabesp
 */
@Repository
public interface PrefeituraRepository extends JpaRepository<Prefeitura, Long> {

    /**
     * Busca uma prefeitura pelo seu e-mail.
     *
     * @param email e-mail da prefeitura
     * @return {@link Optional} contendo a prefeitura, ou vazio se não encontrada
     */
    Optional<Prefeitura> findByEmail(String email);
}

